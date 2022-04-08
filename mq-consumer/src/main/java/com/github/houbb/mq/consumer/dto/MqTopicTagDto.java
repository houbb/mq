package com.github.houbb.mq.consumer.dto;

import java.util.Objects;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqTopicTagDto {

    /**
     * 标题名称
     */
    private String topicName;

    /**
     * 标签名称
     */
    private String tagRegex;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MqTopicTagDto dto = (MqTopicTagDto) object;
        return Objects.equals(topicName, dto.topicName) &&
                Objects.equals(tagRegex, dto.tagRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, tagRegex);
    }
}
