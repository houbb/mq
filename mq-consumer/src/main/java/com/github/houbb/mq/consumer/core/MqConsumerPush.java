package com.github.houbb.mq.consumer.core;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.heaven.util.util.DateUtil;
import com.github.houbb.heaven.util.util.RandomUtil;
import com.github.houbb.id.core.util.IdHelper;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.dto.BrokerRegisterReq;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.MqCommonReq;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.rpc.RpcChannelFuture;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.invoke.impl.InvokeService;
import com.github.houbb.mq.common.util.ChannelFutureUtils;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import com.github.houbb.mq.consumer.api.IMqConsumer;
import com.github.houbb.mq.consumer.api.IMqConsumerListener;
import com.github.houbb.mq.consumer.constant.ConsumerConst;
import com.github.houbb.mq.consumer.constant.ConsumerRespCode;
import com.github.houbb.mq.consumer.handler.MqConsumerHandler;
import com.github.houbb.mq.consumer.support.listener.IMqListenerService;
import com.github.houbb.mq.consumer.support.listener.MqListenerService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final String groupName;

    /**
     * 中间人地址
     */
    private String brokerAddress  = "127.0.0.1:9999";

    /**
     * 调用管理类
     *
     * @since 1.0.0
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 请求列表
     * @since 0.0.3
     */
    private List<RpcChannelFuture> channelFutureList;

    /**
     * 获取响应超时时间
     * @since 0.0.2
     */
    private long respTimeoutMills = 5000;

    /**
     * 可用标识
     * @since 0.0.2
     */
    private volatile boolean enableFlag = false;

    /**
     * 检测 broker 可用性
     * @since 0.0.4
     */
    private volatile boolean check = true;

    /**
     * 消息监听服务类
     * @since 0.0.3
     */
    private final IMqListenerService mqListenerService = new MqListenerService();

    public MqConsumerPush(String groupName) {
        this.groupName = groupName;
    }

    public MqConsumerPush() {
        this(ConsumerConst.DEFAULT_GROUP_NAME);
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    /**
     * 可用状态
     * @return 是否可用
     * @since 0.0.3
     */
    public boolean enableStatus() {
        return enableFlag;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    private ChannelHandler initChannelHandler() {
        final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);

        final MqConsumerHandler mqConsumerHandler = new MqConsumerHandler(invokeService, mqListenerService);
        // handler 实际上会被多次调用，如果不是 @Shareable，应该每次都重新创建。
        ChannelHandler handler = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                        .addLast(mqConsumerHandler);
            }
        };

        return handler;
    }

    /**
     * 参数校验
     */
    private void paramCheck() {
        ArgUtil.notEmpty(brokerAddress, "brokerAddress");
    }

    /**
     * 注册到所有的服务端
     * @since 0.0.3
     */
    private void registerToBroker() {
        for(RpcChannelFuture channelFuture : this.channelFutureList) {
            ServiceEntry serviceEntry = new ServiceEntry();
            serviceEntry.setGroupName(groupName);
            serviceEntry.setAddress(channelFuture.getAddress());
            serviceEntry.setPort(channelFuture.getPort());
            serviceEntry.setWeight(channelFuture.getWeight());

            BrokerRegisterReq brokerRegisterReq = new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);
            brokerRegisterReq.setMethodType(MethodType.C_REGISTER);
            brokerRegisterReq.setTraceId(IdHelper.uuid32());

            log.info("[Register] 开始注册到 broker：{}", JSON.toJSON(brokerRegisterReq));
            final Channel channel = channelFuture.getChannelFuture().channel();
            MqCommonResp resp = callServer(channel, brokerRegisterReq, MqCommonResp.class);
            log.info("[Register] 完成注册到 broker：{}", JSON.toJSON(resp));
        }
    }

    @Override
    public void run() {
        // 启动服务端
        log.info("MQ 消费者开始启动服务端 groupName: {}, brokerAddress: {}",
                groupName, brokerAddress);

        //1. 参数校验
        this.paramCheck();

        try {
            //channel future
            this.channelFutureList = ChannelFutureUtils.initChannelFutureList(brokerAddress,
                    initChannelHandler(),
                    check);

            // register to broker
            this.registerToBroker();

            // 标识为可用
            enableFlag = true;
            log.info("MQ 消费者启动完成");
        } catch (Exception e) {
            log.error("MQ 消费者启动异常", e);
            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        }
    }


    @Override
    public void subscribe(String topicName, String tagRegex) {
        ConsumerSubscribeReq req = new ConsumerSubscribeReq();

        String messageId = IdHelper.uuid32();
        req.setTraceId(messageId);
        req.setMethodType(MethodType.C_SUBSCRIBE);
        req.setTopicName(topicName);
        req.setTagRegex(tagRegex);
        req.setGroupName(groupName);

        Channel channel = getChannel();
        MqCommonResp resp = callServer(channel, req, MqCommonResp.class);
        if(!MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
            throw new MqException(ConsumerRespCode.SUBSCRIBE_FAILED);
        }
    }

    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        ConsumerUnSubscribeReq req = new ConsumerUnSubscribeReq();

        String messageId = IdHelper.uuid32();
        req.setTraceId(messageId);
        req.setMethodType(MethodType.C_UN_SUBSCRIBE);
        req.setTopicName(topicName);
        req.setTagRegex(tagRegex);
        req.setGroupName(groupName);

        Channel channel = getChannel();
        MqCommonResp resp = callServer(channel, req, MqCommonResp.class);
        if(!MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
            throw new MqException(ConsumerRespCode.UN_SUBSCRIBE_FAILED);
        }
    }

    @Override
    public void registerListener(IMqConsumerListener listener) {
        this.mqListenerService.register(listener);
    }

    /**
     * 调用服务端
     * @param channel 调用通道
     * @param commonReq 通用请求
     * @param respClass 类
     * @param <T> 泛型
     * @param <R> 结果
     * @return 结果
     * @since 1.0.0
     */
    private <T extends MqCommonReq, R extends MqCommonResp> R callServer(Channel channel,
                                                                        T commonReq,
                                                                        Class<R> respClass) {
        final String traceId = commonReq.getTraceId();
        final long requestTime = System.currentTimeMillis();

        RpcMessageDto rpcMessageDto = new RpcMessageDto();
        rpcMessageDto.setTraceId(traceId);
        rpcMessageDto.setRequestTime(requestTime);
        rpcMessageDto.setJson(JSON.toJSONString(commonReq));
        rpcMessageDto.setMethodType(commonReq.getMethodType());
        rpcMessageDto.setRequest(true);

        // 添加调用服务
        invokeService.addRequest(traceId, respTimeoutMills);

        // 遍历 channel
        // 关闭当前线程，以获取对应的信息
        // 使用序列化的方式
        ByteBuf byteBuf = DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);

        //负载均衡获取 channel
        channel.writeAndFlush(byteBuf);

        String channelId = ChannelUtil.getChannelId(channel);
        log.debug("[Client] channelId {} 发送消息 {}", channelId, JSON.toJSON(rpcMessageDto));
//        channel.closeFuture().syncUninterruptibly();

        if (respClass == null) {
            log.debug("[Client] 当前消息为 one-way 消息，忽略响应");
            return null;
        } else {
            //channelHandler 中获取对应的响应
            RpcMessageDto messageDto = invokeService.getResponse(traceId);
            if (MqCommonRespCode.TIMEOUT.getCode().equals(messageDto.getRespCode())) {
                throw new MqException(MqCommonRespCode.TIMEOUT);
            }

            String respJson = messageDto.getJson();
            log.info("响应结果：{}", respJson);
            return JSON.parseObject(respJson, respClass);
        }
    }

    /**
     * 获取请求通道
     * TODO: 负载均衡
     * @return 结果
     */
    private Channel getChannel() {
        // 等待启动完成
        while (!enableFlag) {
            log.debug("等待初始化完成...");
            DateUtil.sleep(100);
        }

        RpcChannelFuture rpcChannelFuture = RandomUtil.random(channelFutureList);
        return rpcChannelFuture.getChannelFuture().channel();
    }


}
