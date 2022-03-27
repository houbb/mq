package com.github.houbb.mq.test.producer;

import com.github.houbb.mq.producer.core.MqProducer;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class ProducerMain {

    public static void main(String[] args) {
        MqProducer mqProducer = new MqProducer();
        mqProducer.start();
    }

}
