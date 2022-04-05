package com.github.houbb.mq.common.dto.req;

import java.util.List;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqConsumerPullReq extends MqCommonReq {

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 标题名称
     */
    private String topic;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 拉取大小
     */
    private int size;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "MqConsumerPullReq{" +
                "topic='" + topic + '\'' +
                ", tags=" + tags +
                ", size=" + size +
                "} " + super.toString();
    }

}
