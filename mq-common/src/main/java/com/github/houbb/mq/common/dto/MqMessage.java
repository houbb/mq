package com.github.houbb.mq.common.dto;

import java.util.Arrays;
import java.util.List;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqMessage {

    /**
     * 标题名称
     */
    private String topic;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 内容
     */
    private byte[] payload;

    /**
     * 业务标识
     */
    private String bizKey;

    /**
     * 负载分片标识
     */
    private String shardingKey;

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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    @Override
    public String toString() {
        return "MqMessage{" +
                "topic='" + topic + '\'' +
                ", tags=" + tags +
                ", payload=" + Arrays.toString(payload) +
                ", bizKey='" + bizKey + '\'' +
                ", shardingKey='" + shardingKey + '\'' +
                '}';
    }

}
