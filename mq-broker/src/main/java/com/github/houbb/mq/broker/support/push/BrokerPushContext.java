package com.github.houbb.mq.broker.support.push;

import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import io.netty.channel.Channel;

import java.util.List;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class BrokerPushContext {

    private IMqBrokerPersist mqBrokerPersist;

    private MqMessage mqMessage;

    private List<Channel> channelList;

    private IInvokeService invokeService;

    /**
     * 获取响应超时时间
     * @since 0.0.3
     */
    private long respTimeoutMills;


    public IMqBrokerPersist getMqBrokerPersist() {
        return mqBrokerPersist;
    }

    public void setMqBrokerPersist(IMqBrokerPersist mqBrokerPersist) {
        this.mqBrokerPersist = mqBrokerPersist;
    }

    public MqMessage getMqMessage() {
        return mqMessage;
    }

    public void setMqMessage(MqMessage mqMessage) {
        this.mqMessage = mqMessage;
    }

    public List<Channel> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public IInvokeService getInvokeService() {
        return invokeService;
    }

    public void setInvokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    public long getRespTimeoutMills() {
        return respTimeoutMills;
    }

    public void setRespTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
    }
}
