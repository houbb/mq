package com.github.houbb.mq.consumer.api;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public interface IMqConsumer {

    /**
     * 订阅
     * @param topicName topic 名称
     * @param tagRegex 标签正则
     */
    void subscribe(String topicName, String tagRegex);

    /**
     * 注册监听器
     * @param listener 监听器
     */
    void registerListener(final IMqConsumerListener listener);

}
