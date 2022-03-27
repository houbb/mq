package com.github.houbb.mq.common.dto;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqMessageEx extends MqMessage {

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 请求时间
     */
    private long requestTime;

    /**
     * 生产者地址
     */
    private String producerAddress;

    /**
     * 生产者端口号
     */
    private int producerPort;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getProducerAddress() {
        return producerAddress;
    }

    public void setProducerAddress(String producerAddress) {
        this.producerAddress = producerAddress;
    }

    public int getProducerPort() {
        return producerPort;
    }

    public void setProducerPort(int producerPort) {
        this.producerPort = producerPort;
    }
}
