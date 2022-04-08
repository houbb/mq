package com.github.houbb.mq.broker.handler;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.api.IBrokerConsumerService;
import com.github.houbb.mq.broker.api.IBrokerProducerService;
import com.github.houbb.mq.broker.constant.MessageStatusConst;
import com.github.houbb.mq.broker.dto.BrokerRegisterReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPut;
import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.broker.support.push.BrokerPushContext;
import com.github.houbb.mq.broker.support.push.IBrokerPushService;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.MqConsumerPullReq;
import com.github.houbb.mq.common.dto.req.MqHeartBeatReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqBrokerHandler extends SimpleChannelInboundHandler {

    private static final Log log = LogFactory.getLog(MqBrokerHandler.class);

    /**
     * 调用管理类
     * @since 1.0.0
     */
    private IInvokeService invokeService;

    /**
     * 消费者管理
     * @since 0.0.3
     */
    private IBrokerConsumerService registerConsumerService;

    /**
     * 生产者管理
     * @since 0.0.3
     */
    private IBrokerProducerService registerProducerService;

    /**
     * 持久化类
     * @since 0.0.3
     */
    private IMqBrokerPersist mqBrokerPersist;

    /**
     * 推送服务
     * @since 0.0.3
     */
    private IBrokerPushService brokerPushService;

    /**
     * 获取响应超时时间
     * @since 0.0.3
     */
    private long respTimeoutMills;

    /**
     * 推送最大尝试次数
     * @since 0.0.8
     */
    private int pushMaxAttempt;

    public MqBrokerHandler invokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }

    public MqBrokerHandler registerConsumerService(IBrokerConsumerService registerConsumerService) {
        this.registerConsumerService = registerConsumerService;
        return this;
    }

    public MqBrokerHandler registerProducerService(IBrokerProducerService registerProducerService) {
        this.registerProducerService = registerProducerService;
        return this;
    }

    public MqBrokerHandler mqBrokerPersist(IMqBrokerPersist mqBrokerPersist) {
        this.mqBrokerPersist = mqBrokerPersist;
        return this;
    }

    public MqBrokerHandler brokerPushService(IBrokerPushService brokerPushService) {
        this.brokerPushService = brokerPushService;
        return this;
    }

    public MqBrokerHandler respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqBrokerHandler pushMaxAttempt(int pushMaxAttempt) {
        this.pushMaxAttempt = pushMaxAttempt;
        return this;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        RpcMessageDto rpcMessageDto = null;
        try {
            rpcMessageDto = JSON.parseObject(bytes, RpcMessageDto.class);
        } catch (Exception exception) {
            log.error("RpcMessageDto json 格式转换异常 {}", new String(bytes));
            return;
        }

        if (rpcMessageDto.isRequest()) {
            MqCommonResp commonResp = this.dispatch(rpcMessageDto, ctx);

            if(commonResp == null) {
                log.debug("当前消息为 null，忽略处理。");
                return;
            }

            writeResponse(rpcMessageDto, commonResp, ctx);
        } else {
            final String traceId = rpcMessageDto.getTraceId();

            // 丢弃掉 traceId 为空的信息
            if(StringUtil.isBlank(traceId)) {
                log.debug("[Server Response] response traceId 为空，直接丢弃", JSON.toJSON(rpcMessageDto));
                return;
            }

            // 添加消息
            invokeService.addResponse(traceId, rpcMessageDto);
        }
    }

    /**
     * 消息的分发
     *
     * @param rpcMessageDto 入参
     * @param ctx 上下文
     * @return 结果
     */
    private MqCommonResp dispatch(RpcMessageDto rpcMessageDto, ChannelHandlerContext ctx) {
        try {
            final String methodType = rpcMessageDto.getMethodType();
            final String json = rpcMessageDto.getJson();

            String channelId = ChannelUtil.getChannelId(ctx);
            final Channel channel = ctx.channel();
            log.debug("channelId: {} 接收到 method: {} 内容：{}", channelId,
                    methodType, json);

            // 生产者注册
            if(MethodType.P_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerProducerService.register(registerReq.getServiceEntry(), channel);
            }
            // 生产者注销
            if(MethodType.P_UN_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerProducerService.unRegister(registerReq.getServiceEntry(), channel);
            }
            // 生产者消息发送
            if(MethodType.P_SEND_MSG.equals(methodType)) {
                MqMessage mqMessage = JSON.parseObject(json, MqMessage.class);

                MqMessagePersistPut persistPut = new MqMessagePersistPut();
                persistPut.setMqMessage(mqMessage);
                persistPut.setMessageStatus(MessageStatusConst.WAIT_CONSUMER);
                MqCommonResp commonResp = mqBrokerPersist.put(persistPut);
                this.asyncHandleMessage(mqMessage);
                return commonResp;
            }
            // 生产者消息发送-ONE WAY
            if(MethodType.P_SEND_MSG_ONE_WAY.equals(methodType)) {
                MqMessage mqMessage = JSON.parseObject(json, MqMessage.class);
                MqMessagePersistPut persistPut = new MqMessagePersistPut();
                persistPut.setMqMessage(mqMessage);
                persistPut.setMessageStatus(MessageStatusConst.WAIT_CONSUMER);
                mqBrokerPersist.put(persistPut);
                this.asyncHandleMessage(mqMessage);
                return null;
            }

            // 消费者注册
            if(MethodType.C_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerConsumerService.register(registerReq.getServiceEntry(), channel);
            }
            // 消费者注销
            if(MethodType.C_UN_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerConsumerService.unRegister(registerReq.getServiceEntry(), channel);
            }
            // 消费者监听注册
            if(MethodType.C_SUBSCRIBE.equals(methodType)) {
                ConsumerSubscribeReq req = JSON.parseObject(json, ConsumerSubscribeReq.class);
                return registerConsumerService.subscribe(req, channel);
            }
            // 消费者监听注销
            if(MethodType.C_UN_SUBSCRIBE.equals(methodType)) {
                ConsumerUnSubscribeReq req = JSON.parseObject(json, ConsumerUnSubscribeReq.class);
                return registerConsumerService.unSubscribe(req, channel);
            }
            // 消费者主动 pull
            if(MethodType.C_MESSAGE_PULL.equals(methodType)) {
                MqConsumerPullReq req = JSON.parseObject(json, MqConsumerPullReq.class);
                return mqBrokerPersist.pull(req, channel);
            }
            // 消费者心跳
            if(MethodType.C_HEARTBEAT.equals(methodType)) {
                MqHeartBeatReq req = JSON.parseObject(json, MqHeartBeatReq.class);
                registerConsumerService.heartbeat(req, channel);
                return null;
            }

            throw new UnsupportedOperationException("暂不支持的方法类型");
        } catch (Exception exception) {
            log.error("执行异常", exception);
            MqCommonResp resp = new MqCommonResp();
            resp.setRespCode(MqCommonRespCode.FAIL.getCode());
            resp.setRespMessage(MqCommonRespCode.FAIL.getMsg());
            return resp;
        }
    }

    /**
     * 异步处理消息
     * @param mqMessage 消息
     * @since 0.0.3
     */
    private void asyncHandleMessage(MqMessage mqMessage) {
        List<Channel> channelList = registerConsumerService.getPushSubscribeList(mqMessage);
        if(CollectionUtil.isEmpty(channelList)) {
            log.info("监听列表为空，忽略处理");
            return;
        }

        BrokerPushContext brokerPushContext = BrokerPushContext.newInstance()
                .channelList(channelList)
                .mqMessage(mqMessage)
                .mqBrokerPersist(mqBrokerPersist)
                .invokeService(invokeService)
                .respTimeoutMills(respTimeoutMills)
                .pushMaxAttempt(pushMaxAttempt);

        brokerPushService.asyncPush(brokerPushContext);
    }

    /**
     * 结果写回
     *
     * @param req  请求
     * @param resp 响应
     * @param ctx  上下文
     */
    private void writeResponse(RpcMessageDto req,
                               Object resp,
                               ChannelHandlerContext ctx) {
        final String id = ctx.channel().id().asLongText();

        RpcMessageDto rpcMessageDto = new RpcMessageDto();
        // 响应类消息
        rpcMessageDto.setRequest(false);
        rpcMessageDto.setTraceId(req.getTraceId());
        rpcMessageDto.setMethodType(req.getMethodType());
        rpcMessageDto.setRequestTime(System.currentTimeMillis());
        String json = JSON.toJSONString(resp);
        rpcMessageDto.setJson(json);

        // 回写到 client 端
        ByteBuf byteBuf = DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);
        ctx.writeAndFlush(byteBuf);
        log.debug("[Server] channel {} response {}", id, JSON.toJSON(rpcMessageDto));
    }


}
