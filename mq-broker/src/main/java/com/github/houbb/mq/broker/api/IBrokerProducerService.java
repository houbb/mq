/*
 * Copyright (c)  2019. houbinbin Inc.
 * rpc All rights reserved.
 */

package com.github.houbb.mq.broker.api;


import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import io.netty.channel.Channel;

/**
 * <p> 生产者注册服务类 </p>
 *
 * <pre> Created: 2019/10/23 9:08 下午  </pre>
 * <pre> Project: rpc  </pre>
 *
 * @author houbinbin
 * @since 0.0.3
 */
public interface IBrokerProducerService {

    /**
     * 注册当前服务信息
     * （1）将该服务通过 {@link ServiceEntry#getGroupName()} 进行分组
     * 订阅了这个 serviceId 的所有客户端
     * @param serviceEntry 注册当前服务信息
     * @param channel channel
     * @since 0.0.8
     */
    MqCommonResp register(final ServiceEntry serviceEntry, Channel channel);

    /**
     * 注销当前服务信息
     * @param serviceEntry 注册当前服务信息
     * @param channel 通道
     * @since 0.0.8
     */
    MqCommonResp unRegister(final ServiceEntry serviceEntry, Channel channel);

    /**
     * 获取服务地址信息
     * @param channelId channel
     * @return 结果
     * @since 0.0.3
     */
    ServiceEntry getServiceEntry(final String channelId);

}
