package com.github.houbb.mq.broker.support.persist;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPut;
import com.github.houbb.mq.broker.utils.InnerRegexUtils;
import com.github.houbb.mq.common.constant.MessageStatusConst;
import com.github.houbb.mq.common.dto.req.MqConsumerPullReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.dto.resp.MqConsumerPullResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地持久化策略
 * @author binbin.hou
 * @since 1.0.0
 */
public class LocalMqBrokerPersist implements IMqBrokerPersist {

    private static final Log log = LogFactory.getLog(LocalMqBrokerPersist.class);

    /**
     * 队列
     * ps: 这里只是简化实现，暂时不考虑并发等问题。
     */
    private final Map<String, List<MqMessagePersistPut>> map = new ConcurrentHashMap<>();

    //1. 接收
    //2. 持久化
    //3. 通知消费
    @Override
    public synchronized MqCommonResp put(MqMessagePersistPut put) {
        log.info("put elem: {}", JSON.toJSON(put));

        MqMessage mqMessage = put.getMqMessage();
        final String topic = mqMessage.getTopic();

        List<MqMessagePersistPut> list = map.get(topic);
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(put);
        map.put(topic, list);

        MqCommonResp commonResp = new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }

    @Override
    public MqCommonResp updateStatus(String messageId, String status) {
        // 这里性能比较差，所以不可以用于生产。仅作为测试验证
        for(List<MqMessagePersistPut> list : map.values()) {
            for(MqMessagePersistPut put : list) {
                MqMessage mqMessage = put.getMqMessage();
                if(mqMessage.getTraceId().equals(messageId)) {
                    put.setMessageStatus(status);

                    break;
                }
            }
        }

        MqCommonResp commonResp = new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }

    @Override
    public MqConsumerPullResp pull(MqConsumerPullReq pullReq, Channel channel) {
        //1. 拉取匹配的信息
        //2. 状态更新为代理中
        //3. 如何更新对应的消费状态呢？

        // 获取状态为 W 的订单
        final int fetchSize = pullReq.getSize();
        final String topic = pullReq.getTopicName();
        final String tagRegex = pullReq.getTagRegex();

        List<MqMessage> resultList = new ArrayList<>(fetchSize);
        List<MqMessagePersistPut> putList = map.get(topic);
        // 性能比较差
        if(CollectionUtil.isNotEmpty(putList)) {
            for(MqMessagePersistPut put : putList) {
                if(!isEnableStatus(put)) {
                    continue;
                }

                final MqMessage mqMessage = put.getMqMessage();
                List<String> tagList = mqMessage.getTags();
                if(InnerRegexUtils.hasMatch(tagList, tagRegex)) {
                    // 设置为处理中
                    // TODO： 消息的最终状态什么时候更新呢？
                    // 可以给 broker 一个 ACK
                    put.setMessageStatus(MessageStatusConst.TO_CONSUMER_PROCESS);
                    resultList.add(mqMessage);
                }

                if(resultList.size() >= fetchSize) {
                    break;
                }
            }
        }

        MqConsumerPullResp resp = new MqConsumerPullResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        resp.setList(resultList);
        return resp;
    }

    private boolean isEnableStatus(final MqMessagePersistPut persistPut) {
        final String status = persistPut.getMessageStatus();
        // 数据库可以设计一个字段，比如待消费时间，进行排序。
        // 这里只是简化实现，仅用于测试。
        List<String> statusList = Arrays.asList(MessageStatusConst.WAIT_CONSUMER,
                MessageStatusConst.CONSUMER_LATER);
        return statusList.contains(status);
    }

}
