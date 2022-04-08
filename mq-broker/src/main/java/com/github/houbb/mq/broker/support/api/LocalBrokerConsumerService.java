package com.github.houbb.mq.broker.support.api;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.api.IBrokerConsumerService;
import com.github.houbb.mq.broker.dto.BrokerServiceEntryChannel;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeBo;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.github.houbb.mq.broker.utils.InnerChannelUtils;
import com.github.houbb.mq.broker.utils.InnerRegexUtils;
import com.github.houbb.mq.common.constant.ConsumerTypeConst;
import com.github.houbb.mq.common.dto.req.MqHeartBeatReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.RandomUtils;
import com.github.houbb.mq.common.util.RegexUtils;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class LocalBrokerConsumerService implements IBrokerConsumerService {

    private static final Log log = LogFactory.getLog(LocalBrokerConsumerService.class);

    private final Map<String, BrokerServiceEntryChannel> registerMap = new ConcurrentHashMap<>();

    /**
     * 订阅集合-推送策略
     * key: topicName
     * value: 对应的订阅列表
     */
    private final Map<String, Set<ConsumerSubscribeBo>> pushSubscribeMap = new ConcurrentHashMap<>();

    /**
     * 心跳 map
     * @since 0.0.6
     */
    private final Map<String, BrokerServiceEntryChannel> heartbeatMap = new ConcurrentHashMap<>();

    /**
     * 心跳定时任务
     *
     * @since 0.0.6
     */
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 负载均衡策略
     * @since 0.0.7
     */
    private ILoadBalance<ConsumerSubscribeBo> loadBalance;

    public LocalBrokerConsumerService() {
        //120S 扫描一次
        final long limitMills = 2 * 60 * 1000;
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String, BrokerServiceEntryChannel> entry : heartbeatMap.entrySet()) {
                    String key  = entry.getKey();
                    long lastAccessTime = entry.getValue().getLastAccessTime();
                    long currentTime = System.currentTimeMillis();

                    if(currentTime - lastAccessTime > limitMills) {
                        removeByChannelId(key);
                    }
                }
            }
        }, 2 * 60, 2 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void loadBalance(ILoadBalance<ConsumerSubscribeBo> loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public MqCommonResp register(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        BrokerServiceEntryChannel entryChannel = InnerChannelUtils.buildEntryChannel(serviceEntry, channel);
        registerMap.put(channelId, entryChannel);

        entryChannel.setLastAccessTime(System.currentTimeMillis());
        heartbeatMap.put(channelId, entryChannel);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public MqCommonResp unRegister(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        removeByChannelId(channelId);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    /**
     * 根据 channelId 移除信息
     * @param channelId 通道唯一标识
     * @since 0.0.6
     */
    private void removeByChannelId(final String channelId) {
        BrokerServiceEntryChannel channelRegister = registerMap.remove(channelId);
        log.info("移除注册信息 id: {}, channel: {}", channelId, JSON.toJSON(channelRegister));
        BrokerServiceEntryChannel channelHeartbeat = heartbeatMap.remove(channelId);
        log.info("移除心跳信息 id: {}, channel: {}", channelId, JSON.toJSON(channelHeartbeat));
    }

    @Override
    public MqCommonResp subscribe(ConsumerSubscribeReq serviceEntry, Channel clientChannel) {
        final String channelId = ChannelUtil.getChannelId(clientChannel);
        final String topicName = serviceEntry.getTopicName();

        final String consumerType = serviceEntry.getConsumerType();
        Map<String, Set<ConsumerSubscribeBo>> subscribeMap = getSubscribeMapByConsumerType(consumerType);
        Set<ConsumerSubscribeBo> set = subscribeMap.get(topicName);
        if(set == null) {
            set = new HashSet<>();
        }
        ConsumerSubscribeBo subscribeBo = new ConsumerSubscribeBo();
        subscribeBo.setChannelId(channelId);
        subscribeBo.setGroupName(serviceEntry.getGroupName());
        subscribeBo.setTopicName(topicName);
        subscribeBo.setTagRegex(serviceEntry.getTagRegex());
        set.add(subscribeBo);

        subscribeMap.put(topicName, set);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    private Map<String, Set<ConsumerSubscribeBo>> getSubscribeMapByConsumerType(String consumerType) {
        return pushSubscribeMap;
    }

    @Override
    public MqCommonResp unSubscribe(ConsumerUnSubscribeReq serviceEntry, Channel clientChannel) {
        final String channelId = ChannelUtil.getChannelId(clientChannel);
        final String topicName = serviceEntry.getTopicName();
        final String consumerType = serviceEntry.getConsumerType();
        Map<String, Set<ConsumerSubscribeBo>> subscribeMap = getSubscribeMapByConsumerType(consumerType);

        ConsumerSubscribeBo subscribeBo = new ConsumerSubscribeBo();
        subscribeBo.setChannelId(channelId);
        subscribeBo.setGroupName(serviceEntry.getGroupName());
        subscribeBo.setTopicName(topicName);
        subscribeBo.setTagRegex(serviceEntry.getTagRegex());

        // 集合
        Set<ConsumerSubscribeBo> set = subscribeMap.get(topicName);
        if(CollectionUtil.isNotEmpty(set)) {
            set.remove(subscribeBo);
        }

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public List<Channel> getPushSubscribeList(MqMessage mqMessage) {
        final String topicName = mqMessage.getTopic();
        Set<ConsumerSubscribeBo> set = pushSubscribeMap.get(topicName);
        if(CollectionUtil.isEmpty(set)) {
            return Collections.emptyList();
        }

        //2. 获取匹配的 tag 列表
        final List<String> tagNameList = mqMessage.getTags();

        Map<String, List<ConsumerSubscribeBo>> groupMap = new HashMap<>();
        for(ConsumerSubscribeBo bo : set) {
            String tagRegex = bo.getTagRegex();

            if(InnerRegexUtils.hasMatch(tagNameList, tagRegex)) {
                //TODO: 这种设置模式，统一添加处理 heaven
                String groupName = bo.getGroupName();
                List<ConsumerSubscribeBo> list = groupMap.get(groupName);
                if(list == null) {
                    list = new ArrayList<>();
                }
                list.add(bo);

                groupMap.put(groupName, list);
            }
        }

        //3. 按照 groupName 分组之后，每一组只随机返回一个。最好应该调整为以 shardingkey 选择
        final String shardingKey = mqMessage.getShardingKey();
        List<Channel> channelList = new ArrayList<>();

        for(Map.Entry<String, List<ConsumerSubscribeBo>> entry : groupMap.entrySet()) {
            List<ConsumerSubscribeBo> list = entry.getValue();

            ConsumerSubscribeBo bo = RandomUtils.loadBalance(loadBalance, list, shardingKey);
            final String channelId = bo.getChannelId();
            BrokerServiceEntryChannel entryChannel = registerMap.get(channelId);
            if(entryChannel == null) {
                log.warn("channelId: {} 对应的通道信息为空", channelId);
                continue;
            }
            channelList.add(entryChannel.getChannel());
        }

        return channelList;
    }

    @Override
    public void heartbeat(MqHeartBeatReq mqHeartBeatReq, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        log.info("[HEARTBEAT] 接收消费者心跳 {}, channelId: {}",
                JSON.toJSON(mqHeartBeatReq), channelId);

        ServiceEntry serviceEntry = new ServiceEntry();
        serviceEntry.setAddress(mqHeartBeatReq.getAddress());
        serviceEntry.setPort(mqHeartBeatReq.getPort());

        BrokerServiceEntryChannel entryChannel = InnerChannelUtils.buildEntryChannel(serviceEntry, channel);
        entryChannel.setLastAccessTime(mqHeartBeatReq.getTime());

        heartbeatMap.put(channelId, entryChannel);
    }

}
