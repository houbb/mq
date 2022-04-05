package com.github.houbb.mq.broker.support.persist;

import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPut;
import com.github.houbb.mq.common.dto.req.MqConsumerPullReq;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.dto.resp.MqConsumerPullResp;
import io.netty.channel.Channel;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public interface IMqBrokerPersist {

    /**
     * 保存消息
     * @param mqMessage 消息
     * @since 0.0.3
     */
    MqCommonResp put(final MqMessagePersistPut mqMessage);

    /**
     * 更新状态
     * @param messageId 消息唯一标识
     * @param status 状态
     * @return 结果
     * @since 0.0.3
     */
    MqCommonResp updateStatus(final String messageId,
                              final String status);

    /**
     * 拉取消息
     * @param pull 拉取消息
     * @return 结果
     */
    MqConsumerPullResp pull(final MqConsumerPullReq pull, final Channel channel);

}
