package com.github.houbb.mq.producer.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.support.hook.DefaultShutdownHook;
import com.github.houbb.mq.common.support.hook.ShutdownHooks;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.invoke.impl.InvokeService;
import com.github.houbb.mq.common.support.status.IStatusManager;
import com.github.houbb.mq.common.support.status.StatusManager;
import com.github.houbb.mq.producer.api.IMqProducer;
import com.github.houbb.mq.producer.constant.ProducerRespCode;
import com.github.houbb.mq.producer.dto.SendResult;
import com.github.houbb.mq.producer.support.broker.IProducerBrokerService;
import com.github.houbb.mq.producer.support.broker.ProducerBrokerConfig;
import com.github.houbb.mq.producer.support.broker.ProducerBrokerService;

/**
 * 默认 mq 生产者
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqProducer extends Thread implements IMqProducer {

    private static final Log log = LogFactory.getLog(MqProducer.class);

    /**
     * 分组名称
     */
    private String groupName = "DEFAULT";

    /**
     * 中间人地址
     */
    private String brokerAddress  = "127.0.0.1:9999";

    /**
     * 获取响应超时时间
     * @since 0.0.2
     */
    private long respTimeoutMills = 5000;

    /**
     * 检测 broker 可用性
     * @since 0.0.4
     */
    private volatile boolean check = true;

    /**
     * 调用管理服务
     * @since 0.0.2
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 状态管理类
     * @since 0.0.5
     */
    private final IStatusManager statusManager = new StatusManager();

    /**
     * 生产者-中间服务端服务类
     * @since 0.0.5
     */
    private final IProducerBrokerService producerBrokerService = new ProducerBrokerService();

    /**
     * 为剩余的请求等待时间
     * @since 0.0.5
     */
    private long waitMillsForRemainRequest = 60 * 1000;

    public void setGroupName(String groupName) {
        ArgUtil.notEmpty(groupName, "groupName");

        this.groupName = groupName;
    }

    public void setBrokerAddress(String brokerAddress) {
        ArgUtil.notEmpty(brokerAddress, "brokerAddress");

        this.brokerAddress = brokerAddress;
    }

    public void setRespTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void setWaitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
    }

    /**
     * 参数校验
     */
    private void paramCheck() {
        ArgUtil.notEmpty(groupName, "groupName");
        ArgUtil.notEmpty(brokerAddress, "brokerAddress");
    }

    @Override
    public synchronized void run() {
        this.paramCheck();

        // 启动服务端
        log.info("MQ 生产者开始启动客户端 GROUP: {} brokerAddress: {}",
                groupName, brokerAddress);

        try {
            //0. 配置信息
            ProducerBrokerConfig config = ProducerBrokerConfig.newInstance()
                    .groupName(groupName)
                    .brokerAddress(brokerAddress)
                    .check(check)
                    .respTimeoutMills(respTimeoutMills)
                    .invokeService(invokeService)
                    .statusManager(statusManager);

            //1. 初始化
            this.producerBrokerService.initChannelFutureList(config);

            //2. 连接到服务端
            this.producerBrokerService.registerToBroker();

            //3. 标识为可用
            statusManager.status(true);

            //4. 添加钩子函数
            final DefaultShutdownHook rpcShutdownHook = new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.producerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            log.info("MQ 生产者启动完成");
        } catch (Exception e) {
            log.error("MQ 生产者启动遇到异常", e);
            throw new MqException(ProducerRespCode.RPC_INIT_FAILED);
        }
    }

    @Override
    public SendResult send(MqMessage mqMessage) {
        return this.producerBrokerService.send(mqMessage);
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        return this.producerBrokerService.sendOneWay(mqMessage);
    }

}
