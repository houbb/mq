package com.github.houbb.mq.producer.api;

import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.producer.dto.SendBatchResult;
import com.github.houbb.mq.producer.dto.SendResult;

import java.util.List;

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

    /**
     * 同步发送消息-批量
     * @param mqMessageList 消息类型
     * @return 结果
     * @since 0.1.3
     */
    SendBatchResult sendBatch(final List<MqMessage> mqMessageList);

    /**
     * 单向发送消息-批量
     * @param mqMessageList 消息类型
     * @return 结果
     * @since 0.1.3
     */
    SendBatchResult sendOneWayBatch(final List<MqMessage> mqMessageList);

}
