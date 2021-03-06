package com.github.houbb.mq.producer.support.broker;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.DateUtil;
import com.github.houbb.id.core.util.IdHelper;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.dto.BrokerRegisterReq;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.broker.utils.InnerChannelUtils;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.MqCommonReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.req.MqMessageBatchReq;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.rpc.RpcChannelFuture;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.status.IStatusManager;
import com.github.houbb.mq.common.util.ChannelFutureUtils;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import com.github.houbb.mq.common.util.RandomUtils;
import com.github.houbb.mq.producer.constant.ProducerRespCode;
import com.github.houbb.mq.producer.constant.SendStatus;
import com.github.houbb.mq.producer.core.MqProducer;
import com.github.houbb.mq.producer.dto.SendBatchResult;
import com.github.houbb.mq.producer.dto.SendResult;
import com.github.houbb.mq.producer.handler.MqProducerHandler;
import com.github.houbb.sisyphus.core.core.Retryer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author binbin.hou
 * @since 0.0.5
 */
public class ProducerBrokerService implements IProducerBrokerService{

    private static final Log log = LogFactory.getLog(ProducerBrokerService.class);

    /**
     * ????????????
     */
    private String groupName;

    /**
     * ???????????????
     */
    private String brokerAddress;

    /**
     * ??????????????????
     * @since 0.0.2
     */
    private IInvokeService invokeService;

    /**
     * ????????????????????????
     * @since 0.0.2
     */
    private long respTimeoutMills;

    /**
     * ????????????
     * @since 0.0.3
     */
    private List<RpcChannelFuture> channelFutureList;

    /**
     * ?????? broker ?????????
     * @since 0.0.4
     */
    private boolean check;

    /**
     * ????????????
     * @since 0.0.5
     */
    private IStatusManager statusManager;

    /**
     * ??????????????????
     * @since 0.0.7
     */
    private ILoadBalance<RpcChannelFuture> loadBalance;

    /**
     * ??????????????????????????????
     * @since 0.0.8
     */
    private int maxAttempt = 3;

    /**
     * ????????????
     * @since 0.1.4
     */
    private String appKey;

    /**
     * ????????????
     * @since 0.1.4
     */
    private String appSecret;

    @Override
    public void initChannelFutureList(ProducerBrokerConfig config) {
        //1. ???????????????
        this.invokeService = config.invokeService();
        this.check = config.check();
        this.respTimeoutMills = config.respTimeoutMills();
        this.brokerAddress = config.brokerAddress();
        this.groupName = config.groupName();
        this.statusManager = config.statusManager();
        this.loadBalance = config.loadBalance();
        this.maxAttempt = config.maxAttempt();
        this.appKey = config.appKey();
        this.appSecret = config.appSecret();

        //2. ?????????
        this.channelFutureList = ChannelFutureUtils.initChannelFutureList(brokerAddress,
                initChannelHandler(), check);
    }

    private ChannelHandler initChannelHandler() {
        final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);

        final MqProducerHandler mqProducerHandler = new MqProducerHandler();
        mqProducerHandler.setInvokeService(invokeService);

        // handler ?????????????????????????????????????????? @Shareable?????????????????????????????????
        ChannelHandler handler = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                        .addLast(mqProducerHandler);
            }
        };

        return handler;
    }

    @Override
    public void registerToBroker() {
        int successCount = 0;
        for(RpcChannelFuture channelFuture : this.channelFutureList) {
            ServiceEntry serviceEntry = new ServiceEntry();
            serviceEntry.setGroupName(groupName);
            serviceEntry.setAddress(channelFuture.getAddress());
            serviceEntry.setPort(channelFuture.getPort());
            serviceEntry.setWeight(channelFuture.getWeight());

            BrokerRegisterReq brokerRegisterReq = new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);
            brokerRegisterReq.setMethodType(MethodType.P_REGISTER);
            brokerRegisterReq.setTraceId(IdHelper.uuid32());
            brokerRegisterReq.setAppKey(appKey);
            brokerRegisterReq.setAppSecret(appSecret);

            log.info("[Register] ??????????????? broker???{}", JSON.toJSON(brokerRegisterReq));
            final Channel channel = channelFuture.getChannelFuture().channel();
            MqCommonResp resp = callServer(channel, brokerRegisterReq, MqCommonResp.class);
            log.info("[Register] ??????????????? broker???{}", JSON.toJSON(resp));

            if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
                successCount++;
            }
        }

        if(successCount <= 0 && check) {
            log.error("?????? broker ????????????????????????????????? 0");
            throw new MqException(MqCommonRespCode.P_REGISTER_TO_BROKER_FAILED);
        }
    }

    @Override
    public <T extends MqCommonReq, R extends MqCommonResp> R callServer(Channel channel, T commonReq, Class<R> respClass) {
        final String traceId = commonReq.getTraceId();
        final long requestTime = System.currentTimeMillis();

        RpcMessageDto rpcMessageDto = new RpcMessageDto();
        rpcMessageDto.setTraceId(traceId);
        rpcMessageDto.setRequestTime(requestTime);
        rpcMessageDto.setJson(JSON.toJSONString(commonReq));
        rpcMessageDto.setMethodType(commonReq.getMethodType());
        rpcMessageDto.setRequest(true);

        // ??????????????????
        invokeService.addRequest(traceId, respTimeoutMills);

        // ?????? channel
        // ?????????????????????????????????????????????
        // ????????????????????????
        ByteBuf byteBuf = DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);

        //?????????????????? channel
        channel.writeAndFlush(byteBuf);

        String channelId = ChannelUtil.getChannelId(channel);
        log.debug("[Client] channelId {} ???????????? {}", channelId, JSON.toJSON(rpcMessageDto));
//        channel.closeFuture().syncUninterruptibly();

        if (respClass == null) {
            log.debug("[Client] ??????????????? one-way ?????????????????????");
            return null;
        } else {
            //channelHandler ????????????????????????
            RpcMessageDto messageDto = invokeService.getResponse(traceId);
            if (MqCommonRespCode.TIMEOUT.getCode().equals(messageDto.getRespCode())) {
                throw new MqException(MqCommonRespCode.TIMEOUT);
            }

            String respJson = messageDto.getJson();
            return JSON.parseObject(respJson, respClass);
        }
    }

    @Override
    public Channel getChannel(String key) {
        // ??????????????????
        while (!statusManager.status()) {
            if(statusManager.initFailed()) {
                log.error("???????????????");
                throw new MqException(MqCommonRespCode.P_INIT_FAILED);
            }

            log.debug("?????????????????????...");
            DateUtil.sleep(100);
        }

        RpcChannelFuture rpcChannelFuture = RandomUtils.loadBalance(this.loadBalance,
                channelFutureList, key);
        return rpcChannelFuture.getChannelFuture().channel();
    }

    @Override
    public SendResult send(final MqMessage mqMessage) {
        final String messageId = IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MSG);
        mqMessage.setGroupName(groupName);

        return Retryer.<SendResult>newInstance()
                .maxAttempt(maxAttempt)
                .callable(new Callable<SendResult>() {
                    @Override
                    public SendResult call() throws Exception {
                        return doSend(messageId, mqMessage);
                    }
                }).retryCall();
    }

    private SendResult doSend(String messageId, MqMessage mqMessage) {
        log.info("[Producer] ???????????? messageId: {}, mqMessage: {}",
                messageId, JSON.toJSON(mqMessage));

        Channel channel = getChannel(mqMessage.getShardingKey());
        MqCommonResp resp = callServer(channel, mqMessage, MqCommonResp.class);
        if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
            return SendResult.of(messageId, SendStatus.SUCCESS);
        }

        throw new MqException(ProducerRespCode.MSG_SEND_FAILED);
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        String messageId = IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MSG_ONE_WAY);
        mqMessage.setGroupName(groupName);


        Channel channel = getChannel(mqMessage.getShardingKey());
        this.callServer(channel, mqMessage, null);

        return SendResult.of(messageId, SendStatus.SUCCESS);
    }

    @Override
    public SendBatchResult sendBatch(List<MqMessage> mqMessageList) {
        final List<String> messageIdList = this.fillMessageList(mqMessageList);

        final MqMessageBatchReq batchReq = new MqMessageBatchReq();
        batchReq.setMqMessageList(mqMessageList);
        String traceId = IdHelper.uuid32();
        batchReq.setTraceId(traceId);
        batchReq.setMethodType(MethodType.P_SEND_MSG_BATCH);

        return Retryer.<SendBatchResult>newInstance()
                .maxAttempt(maxAttempt)
                .callable(new Callable<SendBatchResult>() {
                    @Override
                    public SendBatchResult call() throws Exception {
                        return doSendBatch(messageIdList, batchReq, false);
                    }
                }).retryCall();
    }

    private SendBatchResult doSendBatch(List<String> messageIdList,
                                   MqMessageBatchReq batchReq,
                                   boolean oneWay) {
        log.info("[Producer] ?????????????????? messageIdList: {}, batchReq: {}, oneWay: {}",
                messageIdList, JSON.toJSON(batchReq), oneWay);

        // ???????????? sharding-key ?????????
        // ?????????????????????
        MqMessage mqMessage = batchReq.getMqMessageList().get(0);
        Channel channel = getChannel(mqMessage.getShardingKey());

        //one-way
        if(oneWay) {
            log.warn("[Producer] ONE-WAY send, ignore result");
            return SendBatchResult.of(messageIdList, SendStatus.SUCCESS);
        }

        MqCommonResp resp = callServer(channel, batchReq, MqCommonResp.class);
        if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
            return SendBatchResult.of(messageIdList, SendStatus.SUCCESS);
        }

        throw new MqException(ProducerRespCode.MSG_SEND_FAILED);
    }

    private List<String> fillMessageList(final List<MqMessage> mqMessageList) {
        List<String> idList = new ArrayList<>(mqMessageList.size());

        for(MqMessage mqMessage : mqMessageList) {
            String messageId = IdHelper.uuid32();
            mqMessage.setTraceId(messageId);
            mqMessage.setGroupName(groupName);

            idList.add(messageId);
        }

        return idList;
    }

    @Override
    public SendBatchResult sendOneWayBatch(List<MqMessage> mqMessageList) {
        List<String> messageIdList = this.fillMessageList(mqMessageList);

        MqMessageBatchReq batchReq = new MqMessageBatchReq();
        batchReq.setMqMessageList(mqMessageList);
        String traceId = IdHelper.uuid32();
        batchReq.setTraceId(traceId);
        batchReq.setMethodType(MethodType.P_SEND_MSG_ONE_WAY_BATCH);

        return doSendBatch(messageIdList, batchReq, true);
    }

    @Override
    public void destroyAll() {
        for(RpcChannelFuture channelFuture : channelFutureList) {
            Channel channel = channelFuture.getChannelFuture().channel();
            final String channelId = ChannelUtil.getChannelId(channel);
            log.info("???????????????{}", channelId);

            ServiceEntry serviceEntry = InnerChannelUtils.buildServiceEntry(channelFuture);

            BrokerRegisterReq brokerRegisterReq = new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);

            String messageId = IdHelper.uuid32();
            brokerRegisterReq.setTraceId(messageId);
            brokerRegisterReq.setMethodType(MethodType.P_UN_REGISTER);

            this.callServer(channel, brokerRegisterReq, null);

            log.info("???????????????{}", channelId);
        }
    }

}
