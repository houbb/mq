package com.github.houbb.mq.consumer.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.support.hook.DefaultShutdownHook;
import com.github.houbb.mq.common.support.hook.ShutdownHooks;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.invoke.impl.InvokeService;
import com.github.houbb.mq.common.support.status.IStatusManager;
import com.github.houbb.mq.common.support.status.StatusManager;
import com.github.houbb.mq.consumer.api.IMqConsumer;
import com.github.houbb.mq.consumer.api.IMqConsumerListener;
import com.github.houbb.mq.consumer.constant.ConsumerConst;
import com.github.houbb.mq.consumer.constant.ConsumerRespCode;
import com.github.houbb.mq.consumer.support.broker.ConsumerBrokerConfig;
import com.github.houbb.mq.consumer.support.broker.ConsumerBrokerService;
import com.github.houbb.mq.consumer.support.broker.IConsumerBrokerService;
import com.github.houbb.mq.consumer.support.listener.IMqListenerService;
import com.github.houbb.mq.consumer.support.listener.MqListenerService;

/**
 * 推送消费策略
 *
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqConsumerPush extends Thread implements IMqConsumer  {

    private static final Log log = LogFactory.getLog(MqConsumerPush.class);

    /**
     * 组名称
     */
    private String groupName = ConsumerConst.DEFAULT_GROUP_NAME;

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
     * 为剩余的请求等待时间
     * @since 0.0.5
     */
    private long waitMillsForRemainRequest = 60 * 1000;

    /**
     * 调用管理类
     *
     * @since 1.0.0
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 消息监听服务类
     * @since 0.0.5
     */
    private final IMqListenerService mqListenerService = new MqListenerService();

    /**
     * 状态管理类
     * @since 0.0.5
     */
    private final IStatusManager statusManager = new StatusManager();

    /**
     * 生产者-中间服务端服务类
     * @since 0.0.5
     */
    private final IConsumerBrokerService consumerBrokerService = new ConsumerBrokerService();

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setWaitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
    }

    public void setRespTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * 参数校验
     */
    private void paramCheck() {
        ArgUtil.notEmpty(brokerAddress, "brokerAddress");
        ArgUtil.notEmpty(groupName, "groupName");
    }

    @Override
    public void run() {
        // 启动服务端
        log.info("MQ 消费者开始启动服务端 groupName: {}, brokerAddress: {}",
                groupName, brokerAddress);

        //1. 参数校验
        this.paramCheck();

        try {
            //0. 配置信息
            ConsumerBrokerConfig config = ConsumerBrokerConfig.newInstance()
                    .groupName(groupName)
                    .brokerAddress(brokerAddress)
                    .check(check)
                    .respTimeoutMills(respTimeoutMills)
                    .invokeService(invokeService)
                    .statusManager(statusManager)
                    .mqListenerService(mqListenerService);

            //1. 初始化
            this.consumerBrokerService.initChannelFutureList(config);

            //2. 连接到服务端
            this.consumerBrokerService.registerToBroker();

            //3. 标识为可用
            statusManager.status(true);

            //4. 添加钩子函数
            final DefaultShutdownHook rpcShutdownHook = new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.consumerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            log.info("MQ 消费者启动完成");
        } catch (Exception e) {
            log.error("MQ 消费者启动异常", e);
            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        }
    }

    @Override
    public void subscribe(String topicName, String tagRegex) {
        consumerBrokerService.subscribe(topicName, tagRegex);
    }

    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        consumerBrokerService.unSubscribe(topicName, tagRegex);
    }

    @Override
    public void registerListener(IMqConsumerListener listener) {
        this.mqListenerService.register(listener);
    }


}
