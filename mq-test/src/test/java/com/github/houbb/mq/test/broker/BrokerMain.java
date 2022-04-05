package com.github.houbb.mq.test.broker;

import com.github.houbb.mq.broker.core.MqBroker;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class BrokerMain {

    public static void main(String[] args) {
        MqBroker broker = new MqBroker();
        broker.start();
    }

}
