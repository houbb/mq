package com.github.houbb.mq.broker.dto.consumer;

import com.github.houbb.mq.common.dto.req.MqCommonReq;

/**
 * 消费者注销入参
 * @author binbin.hou
 * @since 1.0.0
 */
public class ConsumerUnSubscribeReq extends MqCommonReq {

    /**
     * 分组名称
     * @since 0.0.3
     */
    private String groupName;

    /**
     * 标题名称
     */
    private String topicName;

    /**
     * 标签正则
     */
    private String tagRegex;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTagRegex() {
        return tagRegex;
    }

    public void setTagRegex(String tagRegex) {
        this.tagRegex = tagRegex;
    }

}
