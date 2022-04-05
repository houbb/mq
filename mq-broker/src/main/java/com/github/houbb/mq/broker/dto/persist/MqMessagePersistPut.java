package com.github.houbb.mq.broker.dto.persist;

import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.rpc.RpcAddress;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class MqMessagePersistPut {

    /**
     * 消息体
     */
    private MqMessage mqMessage;

    /**
     * 地址信息
     */
    private RpcAddress rpcAddress;

    /**
     * 消息状态
     */
    private String messageStatus;

    public MqMessage getMqMessage() {
        return mqMessage;
    }

    public void setMqMessage(MqMessage mqMessage) {
        this.mqMessage = mqMessage;
    }

    public RpcAddress getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(RpcAddress rpcAddress) {
        this.rpcAddress = rpcAddress;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }
}
