package com.github.houbb.mq.broker.support.valid;

import com.github.houbb.mq.broker.dto.BrokerRegisterReq;

/**
 * 注册验证方法
 *
 * @author binbin.hou
 * @since 0.1.4
 */
public interface IBrokerRegisterValidService {

    /**
     * 生产者验证合法性
     * @param registerReq 注册信息
     * @return 结果
     * @since 0.1.4
     */
    boolean producerValid(BrokerRegisterReq registerReq);

    /**
     * 消费者验证合法性
     * @param registerReq 注册信息
     * @return 结果
     * @since 0.1.4
     */
    boolean consumerValid(BrokerRegisterReq registerReq);

}
