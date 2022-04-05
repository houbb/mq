package com.github.houbb.mq.producer.support.broker;

import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.status.IStatusManager;

/**
 * @author binbin.hou
 * @since 0.0.5
 */
public class ProducerBrokerConfig {

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 中间人地址
     */
    private String brokerAddress;

    /**
     * 调用管理服务
     * @since 0.0.2
     */
    private IInvokeService invokeService;

    /**
     * 获取响应超时时间
     * @since 0.0.2
     */
    private long respTimeoutMills;

    /**
     * 检测 broker 可用性
     * @since 0.0.4
     */
    private boolean check;

    /**
     * 状态管理
     * @since 0.0.5
     */
    private IStatusManager statusManager;

    public static ProducerBrokerConfig newInstance() {
        return new ProducerBrokerConfig();
    }

    public String groupName() {
        return groupName;
    }

    public ProducerBrokerConfig groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String brokerAddress() {
        return brokerAddress;
    }

    public ProducerBrokerConfig brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public IInvokeService invokeService() {
        return invokeService;
    }

    public ProducerBrokerConfig invokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }

    public long respTimeoutMills() {
        return respTimeoutMills;
    }

    public ProducerBrokerConfig respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public boolean check() {
        return check;
    }

    public ProducerBrokerConfig check(boolean check) {
        this.check = check;
        return this;
    }

    public IStatusManager statusManager() {
        return statusManager;
    }

    public ProducerBrokerConfig statusManager(IStatusManager statusManager) {
        this.statusManager = statusManager;
        return this;
    }
}
