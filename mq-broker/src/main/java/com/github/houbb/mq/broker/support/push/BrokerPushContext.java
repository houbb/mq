package com.github.houbb.mq.broker.support.push;

import com.github.houbb.mq.broker.dto.ChannelGroupNameDto;
import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPut;
import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class BrokerPushContext {

    private IMqBrokerPersist mqBrokerPersist;

    private MqMessagePersistPut mqMessagePersistPut;

    private List<ChannelGroupNameDto> channelList;

    private IInvokeService invokeService;

    /**
     * 获取响应超时时间
     * @since 0.0.3
     */
    private long respTimeoutMills;

    /**
     * 推送最大尝试次数
     * @since 0.0.8
     */
    private int pushMaxAttempt;

    /**
     * channel 标识和 groupName map
     * @since 0.1.1
     */
    private Map<String, String> channelGroupMap;

    public static BrokerPushContext newInstance() {
        return new BrokerPushContext();
    }

    public IMqBrokerPersist mqBrokerPersist() {
        return mqBrokerPersist;
    }

    public BrokerPushContext mqBrokerPersist(IMqBrokerPersist mqBrokerPersist) {
        this.mqBrokerPersist = mqBrokerPersist;
        return this;
    }

    public MqMessagePersistPut mqMessagePersistPut() {
        return mqMessagePersistPut;
    }

    public BrokerPushContext mqMessagePersistPut(MqMessagePersistPut mqMessagePersistPut) {
        this.mqMessagePersistPut = mqMessagePersistPut;
        return this;
    }

    public List<ChannelGroupNameDto> channelList() {
        return channelList;
    }

    public BrokerPushContext channelList(List<ChannelGroupNameDto> channelList) {
        this.channelList = channelList;
        return this;
    }

    public IInvokeService invokeService() {
        return invokeService;
    }

    public BrokerPushContext invokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }

    public long respTimeoutMills() {
        return respTimeoutMills;
    }

    public BrokerPushContext respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public int pushMaxAttempt() {
        return pushMaxAttempt;
    }

    public BrokerPushContext pushMaxAttempt(int pushMaxAttempt) {
        this.pushMaxAttempt = pushMaxAttempt;
        return this;
    }

    public Map<String, String> channelGroupMap() {
        return channelGroupMap;
    }

    public BrokerPushContext channelGroupMap(Map<String, String> channelGroupMap) {
        this.channelGroupMap = channelGroupMap;
        return this;
    }
}
