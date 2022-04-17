package com.github.houbb.mq.test.producer;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.DateUtil;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.producer.core.MqProducer;
import com.github.houbb.mq.producer.dto.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class ProducerMain {

    public static void main(String[] args) {
        MqProducer mqProducer = new MqProducer();
        mqProducer.start();

        for(int i = 0; i < 20; i++) {
            MqMessage mqMessage = buildMessage(i);
            SendResult sendResult = mqProducer.send(mqMessage);
            System.out.println(JSON.toJSON(sendResult));
        }
    }

    private static MqMessage buildMessage(int i) {
        String message = "HELLO MQ!" + i;
        MqMessage mqMessage = new MqMessage();
        mqMessage.setTopic("TOPIC");
        mqMessage.setTags(Arrays.asList("TAGA", "TAGB"));
        mqMessage.setPayload(message);

        return mqMessage;
    }

}
