package com.github.houbb.mq.consumer.support.listener;

import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.resp.ConsumerStatus;
import com.github.houbb.mq.consumer.api.IMqConsumerListener;
import com.github.houbb.mq.consumer.api.IMqConsumerListenerContext;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public interface IMqListenerService {

    /**
     * 注册
     * @param listener 监听器
     * @since 0.0.3
     */
    void register(final IMqConsumerListener listener);

    /**
     * 消费消息
     * @param mqMessage 消息
     * @param context 上下文
     * @return 结果
     * @since 0.0.3
     */
    ConsumerStatus consumer(final MqMessage mqMessage,
                            final IMqConsumerListenerContext context);

}
