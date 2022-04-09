package com.github.houbb.mq.common.dto.req;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqConsumerUpdateStatusReq extends MqCommonReq {

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 消息状态
     */
    private String messageStatus;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    @Override
    public String toString() {
        return "MqConsumerUpdateStatusReq{" +
                "messageId='" + messageId + '\'' +
                ", messageStatus='" + messageStatus + '\'' +
                "} " + super.toString();
    }

}
