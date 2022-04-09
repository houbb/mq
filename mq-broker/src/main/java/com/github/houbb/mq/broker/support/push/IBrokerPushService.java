package com.github.houbb.mq.broker.support.push;

/**
 * 消息推送服务
 *
 * @author binbin.hou
 * @since 1.0.0
 */
public interface IBrokerPushService {

    /**
     * 异步推送
     * @param context 消息
     */
    void asyncPush(final BrokerPushContext context);

}
