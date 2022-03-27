package com.github.houbb.mq.consumer.api;

import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.consumer.constant.ConsumerStatus;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public interface IMqConsumerListener {


    /**
     * 消费
     * @param mqMessage 消息体
     * @param context 上下文
     * @return 结果
     */
    ConsumerStatus consumer(final MqMessage mqMessage,
                            final IMqConsumerListenerContext context);

}
