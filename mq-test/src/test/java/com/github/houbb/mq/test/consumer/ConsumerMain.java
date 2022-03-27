package com.github.houbb.mq.test.consumer;

import com.github.houbb.mq.consumer.core.MqConsumerPush;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class ConsumerMain {

    //1. 首先启动消费者，然后启动生产者。
    public static void main(String[] args) {
        MqConsumerPush mqConsumerPush = new MqConsumerPush();
        mqConsumerPush.start();
    }

}
