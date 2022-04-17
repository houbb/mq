package com.github.houbb.mq.common.dto.req;

import java.util.List;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqMessageBatchReq extends MqCommonReq {

    private List<MqMessage> mqMessageList;

    public List<MqMessage> getMqMessageList() {
        return mqMessageList;
    }

    public void setMqMessageList(List<MqMessage> mqMessageList) {
        this.mqMessageList = mqMessageList;
    }
}
