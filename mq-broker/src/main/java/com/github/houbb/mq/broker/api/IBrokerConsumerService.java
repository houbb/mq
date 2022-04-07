/*
 * Copyright (c)  2019. houbinbin Inc.
 * rpc All rights reserved.
 */

package com.github.houbb.mq.broker.api;


import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeBo;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.github.houbb.mq.common.dto.req.MqHeartBeatReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import io.netty.channel.Channel;

import java.util.List;

/**
 * <p> 消费者注册服务类 </p>
 *
 * <pre> Created: 2019/10/23 9:08 下午  </pre>
 * <pre> Project: rpc  </pre>
 *
 * @author houbinbin
 * @since 0.0.3
 */
public interface IBrokerConsumerService {

    /**
     * 设置负载均衡策略
     * @param loadBalance 负载均衡
     * @since 0.0.7
     */
     void loadBalance(ILoadBalance<ConsumerSubscribeBo> loadBalance);

    /**
     * 注册当前服务信息
     * （1）将该服务通过 {@link ServiceEntry#getGroupName()} 进行分组
     * 订阅了这个 serviceId 的所有客户端
     * @param serviceEntry 注册当前服务信息
     * @param channel channel
     * @since 0.0.3
     */
    MqCommonResp register(final ServiceEntry serviceEntry, Channel channel);

    /**
     * 注销当前服务信息
     * @param serviceEntry 注册当前服务信息
     * @param channel channel
     * @since 0.0.3
     */
    MqCommonResp unRegister(final ServiceEntry serviceEntry, Channel channel);

    /**
     * 监听服务信息
     * （1）监听之后，如果有任何相关的机器信息发生变化，则进行推送。
     * （2）内置的信息，需要传送 ip 信息到注册中心。
     *
     * @param serviceEntry 客户端明细信息
     * @param clientChannel 客户端 channel 信息
     * @since 0.0.3
     */
    MqCommonResp subscribe(final ConsumerSubscribeReq serviceEntry,
                   final Channel clientChannel);

    /**
     * 取消监听服务信息
     * （1）监听之后，如果有任何相关的机器信息发生变化，则进行推送。
     * （2）内置的信息，需要传送 ip 信息到注册中心。
     *
     * @param serviceEntry 客户端明细信息
     * @param clientChannel 客户端 channel 信息
     * @since 0.0.3
     */
    MqCommonResp unSubscribe(final ConsumerUnSubscribeReq serviceEntry,
                   final Channel clientChannel);

    /**
     * 获取所有匹配的消费者
     * 1. 同一个 groupName 只返回一个，注意负载均衡
     * 2. 返回匹配当前消息的消费者通道
     *
     * @param mqMessage 消息体
     * @return 结果
     */
    List<Channel> getSubscribeList(MqMessage mqMessage);

    /**
     * 心跳
     * @param mqHeartBeatReq 入参
     * @param channel 渠道
     * @since 0.0.6
     */
    void heartbeat(final MqHeartBeatReq mqHeartBeatReq, Channel channel);

}
