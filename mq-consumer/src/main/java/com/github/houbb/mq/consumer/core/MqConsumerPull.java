package com.github.houbb.mq.consumer.core;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.constant.ConsumerTypeConst;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqConsumerPullResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.consumer.api.IMqConsumerListenerContext;
import com.github.houbb.mq.consumer.dto.MqTopicTagDto;
import com.github.houbb.mq.consumer.support.listener.MqConsumerListenerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 拉取消费策略
 *
 * @author binbin.hou
 * @since 0.0.9
 */
public class MqConsumerPull extends MqConsumerPush  {

    private static final Log log = LogFactory.getLog(MqConsumerPull.class);

    /**
     * 拉取定时任务
     *
     * @since 0.0.9
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 单次拉取大小
     * @since 0.0.9
     */
    private int size = 10;

    /**
     * 初始化延迟毫秒数
     * @since 0.0.9
     */
    private int pullInitDelaySeconds = 5;

    /**
     * 拉取周期
     * @since 0.0.9
     */
    private int pullPeriodSeconds = 5;

    /**
     * 订阅列表
     * @since 0.0.9
     */
    private final List<MqTopicTagDto> subscribeList = new ArrayList<>();

    public MqConsumerPull size(int size) {
        this.size = size;
        return this;
    }

    public MqConsumerPull pullInitDelaySeconds(int pullInitDelaySeconds) {
        this.pullInitDelaySeconds = pullInitDelaySeconds;
        return this;
    }

    public MqConsumerPull pullPeriodSeconds(int pullPeriodSeconds) {
        this.pullPeriodSeconds = pullPeriodSeconds;
        return this;
    }

    /**
     * 初始化拉取消息
     * @since 0.0.6
     */
    @Override
    public void afterInit() {
        //5S 发一次心跳
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(CollectionUtil.isEmpty(subscribeList)) {
                    log.warn("订阅列表为空，忽略处理。");
                    return;
                }

                for(MqTopicTagDto tagDto : subscribeList) {
                    final String topicName = tagDto.getTopicName();
                    final String tagRegex = tagDto.getTagRegex();

                    MqConsumerPullResp resp = consumerBrokerService.pull(topicName, tagRegex, size);

                    if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
                        List<MqMessage> mqMessageList = resp.getList();
                        if(CollectionUtil.isNotEmpty(mqMessageList)) {
                            for(MqMessage mqMessage : mqMessageList) {
                                IMqConsumerListenerContext context = new MqConsumerListenerContext();

                                mqListenerService.consumer(mqMessage, context);
                            }
                        }
                    } else {
                        log.error("拉取消息失败: {}", JSON.toJSON(resp));
                    }
                }
            }
        }, pullInitDelaySeconds, pullPeriodSeconds, TimeUnit.SECONDS);
    }

    @Override
    protected String getConsumerType() {
        return ConsumerTypeConst.PULL;
    }

    @Override
    public synchronized void subscribe(String topicName, String tagRegex) {
        MqTopicTagDto tagDto = buildMqTopicTagDto(topicName, tagRegex);

        if(!subscribeList.contains(tagDto)) {
            subscribeList.add(tagDto);
        }
    }

    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        MqTopicTagDto tagDto = buildMqTopicTagDto(topicName, tagRegex);

        subscribeList.remove(tagDto);
    }

    private MqTopicTagDto buildMqTopicTagDto(String topicName, String tagRegex) {
        MqTopicTagDto dto = new MqTopicTagDto();
        dto.setTagRegex(tagRegex);
        dto.setTopicName(topicName);
        return dto;
    }

}