package com.github.houbb.mq.producer.api;

import com.github.houbb.mq.common.dto.MqMessage;
import com.github.houbb.mq.producer.dto.SendResult;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public interface IMqProducer {

    /**
     * 同步发送消息
     * @param mqMessage 消息类型
     * @return 结果
     */
    SendResult send(final MqMessage mqMessage);

    /**
     * 单向发送消息
     * @param mqMessage 消息类型
     * @return 结果
     */
    SendResult sendOneWay(final MqMessage mqMessage);

}
