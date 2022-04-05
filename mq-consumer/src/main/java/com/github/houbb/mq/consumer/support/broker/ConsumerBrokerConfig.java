package com.github.houbb.mq.consumer.support.broker;

import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.status.IStatusManager;
import com.github.houbb.mq.consumer.support.listener.IMqListenerService;

/**
 * @author binbin.hou
 * @since 0.0.5
 */
public class ConsumerBrokerConfig {

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

    /**
     * 监听服务类
     * @since 0.0.5
     */
    private IMqListenerService mqListenerService;

    public static ConsumerBrokerConfig newInstance() {
        return new ConsumerBrokerConfig();
    }

    public String groupName() {
        return groupName;
    }

    public ConsumerBrokerConfig groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String brokerAddress() {
        return brokerAddress;
    }

    public ConsumerBrokerConfig brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public IInvokeService invokeService() {
        return invokeService;
    }

    public ConsumerBrokerConfig invokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }

    public long respTimeoutMills() {
        return respTimeoutMills;
    }

    public ConsumerBrokerConfig respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public boolean check() {
        return check;
    }

    public ConsumerBrokerConfig check(boolean check) {
        this.check = check;
        return this;
    }

    public IStatusManager statusManager() {
        return statusManager;
    }

    public ConsumerBrokerConfig statusManager(IStatusManager statusManager) {
        this.statusManager = statusManager;
        return this;
    }

    public IMqListenerService mqListenerService() {
        return mqListenerService;
    }

    public ConsumerBrokerConfig mqListenerService(IMqListenerService mqListenerService) {
        this.mqListenerService = mqListenerService;
        return this;
    }
}
