package com.github.houbb.mq.broker.handler;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.api.IBrokerConsumerService;
import com.github.houbb.mq.broker.api.IBrokerProducerService;
import com.github.houbb.mq.broker.dto.BrokerRegisterReq;
import com.github.houbb.mq.broker.dto.ChannelGroupNameDto;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.github.houbb.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPut;
import com.github.houbb.mq.broker.dto.persist.MqMessagePersistPutBatch;
import com.github.houbb.mq.broker.resp.MqBrokerRespCode;
import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.broker.support.push.BrokerPushContext;
import com.github.houbb.mq.broker.support.push.IBrokerPushService;
import com.github.houbb.mq.broker.support.valid.BrokerRegisterValidService;
import com.github.houbb.mq.broker.support.valid.IBrokerRegisterValidService;
import com.github.houbb.mq.common.constant.MessageStatusConst;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.*;
import com.github.houbb.mq.common.dto.req.component.MqConsumerUpdateStatusDto;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqBrokerHandler extends SimpleChannelInboundHandler {

    private static final Log log = LogFactory.getLog(MqBrokerHandler.class);

    /**
     * ???????????????
     * @since 1.0.0
     */
    private IInvokeService invokeService;

    /**
     * ???????????????
     * @since 0.0.3
     */
    private IBrokerConsumerService registerConsumerService;

    /**
     * ???????????????
     * @since 0.0.3
     */
    private IBrokerProducerService registerProducerService;

    /**
     * ????????????
     * @since 0.0.3
     */
    private IMqBrokerPersist mqBrokerPersist;

    /**
     * ????????????
     * @since 0.0.3
     */
    private IBrokerPushService brokerPushService;

    /**
     * ????????????????????????
     * @since 0.0.3
     */
    private long respTimeoutMills;

    /**
     * ????????????????????????
     * @since 0.0.8
     */
    private int pushMaxAttempt;

    /**
     * ?????????????????????
     * @since 0.1.4
     */
    private IBrokerRegisterValidService brokerRegisterValidService;

    public MqBrokerHandler brokerRegisterValidService(IBrokerRegisterValidService brokerRegisterValidService) {
        this.brokerRegisterValidService = brokerRegisterValidService;
        return this;
    }

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
            log.error("RpcMessageDto json ?????????????????? {}", new String(bytes));
            return;
        }

        if (rpcMessageDto.isRequest()) {
            MqCommonResp commonResp = this.dispatch(rpcMessageDto, ctx);

            if(commonResp == null) {
                log.debug("??????????????? null??????????????????");
                return;
            }

            writeResponse(rpcMessageDto, commonResp, ctx);
        } else {
            final String traceId = rpcMessageDto.getTraceId();

            // ????????? traceId ???????????????
            if(StringUtil.isBlank(traceId)) {
                log.debug("[Server Response] response traceId ?????????????????????", JSON.toJSON(rpcMessageDto));
                return;
            }

            // ????????????
            invokeService.addResponse(traceId, rpcMessageDto);
        }
    }

    /**
     * ???????????????
     *
     * @param rpcMessageDto ??????
     * @param ctx ?????????
     * @return ??????
     */
    private MqCommonResp dispatch(RpcMessageDto rpcMessageDto, ChannelHandlerContext ctx) {
        try {
            final String methodType = rpcMessageDto.getMethodType();
            final String json = rpcMessageDto.getJson();

            String channelId = ChannelUtil.getChannelId(ctx);
            final Channel channel = ctx.channel();
            log.debug("channelId: {} ????????? method: {} ?????????{}", channelId,
                    methodType, json);

            // ???????????????
            if(MethodType.P_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                if(!brokerRegisterValidService.producerValid(registerReq)) {
                    log.error("{} ???????????????????????????", JSON.toJSON(registerReq));
                    throw new MqException(MqBrokerRespCode.P_REGISTER_VALID_FAILED);
                }

                return registerProducerService.register(registerReq.getServiceEntry(), channel);
            }
            // ???????????????
            if(MethodType.P_UN_REGISTER.equals(methodType)) {
                registerProducerService.checkValid(channelId);

                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerProducerService.unRegister(registerReq.getServiceEntry(), channel);
            }
            // ?????????????????????
            if(MethodType.P_SEND_MSG.equals(methodType)) {
                registerProducerService.checkValid(channelId);

                return handleProducerSendMsg(channelId, json);
            }
            // ?????????????????????-ONE WAY
            if(MethodType.P_SEND_MSG_ONE_WAY.equals(methodType)) {
                registerProducerService.checkValid(channelId);

                handleProducerSendMsg(channelId, json);

                return null;
            }
            // ?????????????????????-??????
            if(MethodType.P_SEND_MSG_BATCH.equals(methodType)) {
                registerProducerService.checkValid(channelId);

                return handleProducerSendMsgBatch(channelId, json);
            }
            // ?????????????????????-ONE WAY-??????
            if(MethodType.P_SEND_MSG_ONE_WAY_BATCH.equals(methodType)) {
                registerProducerService.checkValid(channelId);

                handleProducerSendMsgBatch(channelId, json);

                return null;
            }

            // ???????????????
            if(MethodType.C_REGISTER.equals(methodType)) {
                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                if(!brokerRegisterValidService.consumerValid(registerReq)) {
                    log.error("{} ???????????????????????????", JSON.toJSON(registerReq));
                    throw new MqException(MqBrokerRespCode.C_REGISTER_VALID_FAILED);
                }

                return registerConsumerService.register(registerReq.getServiceEntry(), channel);
            }
            // ???????????????
            if(MethodType.C_UN_REGISTER.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                BrokerRegisterReq registerReq = JSON.parseObject(json, BrokerRegisterReq.class);
                return registerConsumerService.unRegister(registerReq.getServiceEntry(), channel);
            }
            // ?????????????????????
            if(MethodType.C_SUBSCRIBE.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                ConsumerSubscribeReq req = JSON.parseObject(json, ConsumerSubscribeReq.class);
                return registerConsumerService.subscribe(req, channel);
            }
            // ?????????????????????
            if(MethodType.C_UN_SUBSCRIBE.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                ConsumerUnSubscribeReq req = JSON.parseObject(json, ConsumerUnSubscribeReq.class);
                return registerConsumerService.unSubscribe(req, channel);
            }
            // ??????????????? pull
            if(MethodType.C_MESSAGE_PULL.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                MqConsumerPullReq req = JSON.parseObject(json, MqConsumerPullReq.class);
                return mqBrokerPersist.pull(req, channel);
            }
            // ???????????????
            if(MethodType.C_HEARTBEAT.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                MqHeartBeatReq req = JSON.parseObject(json, MqHeartBeatReq.class);
                registerConsumerService.heartbeat(req, channel);
                return null;
            }
            // ????????????????????? ACK
            if(MethodType.C_CONSUMER_STATUS.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                MqConsumerUpdateStatusReq req = JSON.parseObject(json, MqConsumerUpdateStatusReq.class);
                final String messageId = req.getMessageId();
                final String messageStatus = req.getMessageStatus();
                final String consumerGroupName = req.getConsumerGroupName();
                return mqBrokerPersist.updateStatus(messageId, consumerGroupName, messageStatus);
            }
            //????????????????????? ACK-??????
            if(MethodType.C_CONSUMER_STATUS_BATCH.equals(methodType)) {
                registerConsumerService.checkValid(channelId);

                MqConsumerUpdateStatusBatchReq req = JSON.parseObject(json, MqConsumerUpdateStatusBatchReq.class);
                final List<MqConsumerUpdateStatusDto> statusDtoList = req.getStatusList();
                return mqBrokerPersist.updateStatusBatch(statusDtoList);
            }

            log.error("?????????????????????????????? {}", methodType);
            throw new MqException(MqBrokerRespCode.B_NOT_SUPPORT_METHOD);
        } catch (MqException mqException) {
            log.error("??????????????????", mqException);
            MqCommonResp resp = new MqCommonResp();
            resp.setRespCode(mqException.getCode());
            resp.setRespMessage(mqException.getMsg());
            return resp;
        } catch (Exception exception) {
            log.error("????????????", exception);
            MqCommonResp resp = new MqCommonResp();
            resp.setRespCode(MqCommonRespCode.FAIL.getCode());
            resp.setRespMessage(MqCommonRespCode.FAIL.getMsg());
            return resp;
        }
    }


    /**
     *  ??????????????????????????????
     *
     * @param json ?????????
     * @since 0.1.1
     */
    private MqCommonResp handleProducerSendMsg(String channelId, String json) {
        MqMessage mqMessage = JSON.parseObject(json, MqMessage.class);
        MqMessagePersistPut persistPut = new MqMessagePersistPut();
        persistPut.setMqMessage(mqMessage);
        persistPut.setMessageStatus(MessageStatusConst.WAIT_CONSUMER);
        // ?????? rpc ??????
        final ServiceEntry serviceEntry = registerProducerService.getServiceEntry(channelId);
        persistPut.setRpcAddress(serviceEntry);

        MqCommonResp commonResp = mqBrokerPersist.put(persistPut);
        this.asyncHandleMessage(persistPut);
        return commonResp;
    }

    /**
     * ??????????????????????????????
     *
     * @param channelId ????????????
     * @param json ?????????
     * @since 0.1.3
     */
    private MqCommonResp handleProducerSendMsgBatch(String channelId, String json) {
        MqMessageBatchReq batchReq = JSON.parseObject(json, MqMessageBatchReq.class);
        final ServiceEntry serviceEntry = registerProducerService.getServiceEntry(channelId);

        List<MqMessagePersistPut> putList = buildPersistPutList(batchReq, serviceEntry);

        MqCommonResp commonResp = mqBrokerPersist.putBatch(putList);

        // ??????????????????
        for(MqMessagePersistPut persistPut : putList) {
            this.asyncHandleMessage(persistPut);
        }

        return commonResp;
    }

    /**
     * ????????????
     * @param batchReq ??????
     * @param serviceEntry ??????
     * @return ??????
     */
    private List<MqMessagePersistPut> buildPersistPutList(MqMessageBatchReq batchReq,
                                                          final ServiceEntry serviceEntry) {
        List<MqMessagePersistPut> resultList = new ArrayList<>();

        // ????????????
        List<MqMessage> messageList = batchReq.getMqMessageList();
        for(MqMessage mqMessage : messageList) {
            MqMessagePersistPut put = new MqMessagePersistPut();
            put.setRpcAddress(serviceEntry);
            put.setMessageStatus(MessageStatusConst.WAIT_CONSUMER);
            put.setMqMessage(mqMessage);

            resultList.add(put);
        }

        return resultList;
    }

    /**
     * ??????????????????
     * @param put ??????
     * @since 0.0.3
     */
    private void asyncHandleMessage(MqMessagePersistPut put) {
        final MqMessage mqMessage = put.getMqMessage();
        List<ChannelGroupNameDto> channelList = registerConsumerService.getPushSubscribeList(mqMessage);
        if(CollectionUtil.isEmpty(channelList)) {
            log.info("?????????????????????????????????");
            return;
        }

        BrokerPushContext brokerPushContext = BrokerPushContext.newInstance()
                .channelList(channelList)
                .mqMessagePersistPut(put)
                .mqBrokerPersist(mqBrokerPersist)
                .invokeService(invokeService)
                .respTimeoutMills(respTimeoutMills)
                .pushMaxAttempt(pushMaxAttempt);

        brokerPushService.asyncPush(brokerPushContext);
    }

    /**
     * ????????????
     *
     * @param req  ??????
     * @param resp ??????
     * @param ctx  ?????????
     */
    private void writeResponse(RpcMessageDto req,
                               Object resp,
                               ChannelHandlerContext ctx) {
        final String id = ctx.channel().id().asLongText();

        RpcMessageDto rpcMessageDto = new RpcMessageDto();
        // ???????????????
        rpcMessageDto.setRequest(false);
        rpcMessageDto.setTraceId(req.getTraceId());
        rpcMessageDto.setMethodType(req.getMethodType());
        rpcMessageDto.setRequestTime(System.currentTimeMillis());
        String json = JSON.toJSONString(resp);
        rpcMessageDto.setJson(json);

        // ????????? client ???
        ByteBuf byteBuf = DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);
        ctx.writeAndFlush(byteBuf);
        log.debug("[Server] channel {} response {}", id, JSON.toJSON(rpcMessageDto));
    }


}
