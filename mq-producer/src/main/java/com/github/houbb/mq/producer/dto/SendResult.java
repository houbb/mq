package com.github.houbb.mq.producer.dto;

import com.github.houbb.mq.producer.constant.SendStatus;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class SendResult {

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 发送状态
     */
    private SendStatus status;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public SendStatus getStatus() {
        return status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }
}
