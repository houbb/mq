package com.github.houbb.mq.test.consumer;

import com.github.houbb.mq.consumer.core.MqConsumerPush;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class ConsumerMain {

    public static void main(String[] args) {
        MqConsumerPush mqConsumerPush = new MqConsumerPush();
        mqConsumerPush.start();
    }

}
