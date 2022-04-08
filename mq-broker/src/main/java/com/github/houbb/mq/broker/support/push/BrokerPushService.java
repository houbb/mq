package com.github.houbb.mq.broker.support.push;

import com.alibaba.fastjson.JSON;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.constant.BrokerRespCode;
import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.MqCommonReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.dto.resp.MqConsumerResultResp;
import com.github.houbb.mq.common.resp.ConsumerStatus;
import com.github.houbb.mq.common.resp.MqCommonRespCode;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import com.github.houbb.sisyphus.core.core.Retryer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class BrokerPushService implements IBrokerPushService {

    private static final Log log = LogFactory.getLog(BrokerPushService.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public void asyncPush(final BrokerPushContext context) {
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                log.info("开始异步处理 {}", JSON.toJSON(context));
                final List<Channel> channelList = context.channelList();
                final IMqBrokerPersist mqBrokerPersist = context.mqBrokerPersist();
                final MqMessage mqMessage = context.mqMessage();
                final String messageId = mqMessage.getTraceId();
                final IInvokeService invokeService = context.invokeService();
                final long responseTime = context.respTimeoutMills();
                final int pushMaxAttempt = context.pushMaxAttempt();

                for(final Channel channel : channelList) {
                    try {
                        String channelId = ChannelUtil.getChannelId(channel);

                        log.info("开始处理 channelId: {}", channelId);
                        //1. 调用
                        mqMessage.setMethodType(MethodType.B_MESSAGE_PUSH);

                        // 重试推送
                        MqConsumerResultResp resultResp = Retryer.<MqConsumerResultResp>newInstance()
                                .maxAttempt(pushMaxAttempt)
                                .callable(new Callable<MqConsumerResultResp>() {
                                    @Override
                                    public MqConsumerResultResp call() throws Exception {
                                        MqConsumerResultResp resp = callServer(channel, mqMessage,
                                                MqConsumerResultResp.class, invokeService, responseTime);

                                        // 失败校验
                                        if(resp == null
                                            || !ConsumerStatus.SUCCESS.getCode()
                                                .equals(resp.getConsumerStatus())) {
                                            throw new MqException(BrokerRespCode.MSG_PUSH_FAILED);
                                        }
                                        return resp;
                                    }
                                }).retryCall();

                        //2. 更新状态
                        mqBrokerPersist.updateStatus(messageId, resultResp.getConsumerStatus());

                        //3. 后期添加重试策略

                        log.info("完成处理 channelId: {}", channelId);
                    } catch (Exception exception) {
                        log.error("处理异常");
                        mqBrokerPersist.updateStatus(messageId, ConsumerStatus.FAILED.getCode());
                    }
                }

                log.info("完成异步处理");
            }
        });
    }

    /**
     * 调用服务端
     * @param channel 调用通道
     * @param commonReq 通用请求
     * @param respClass 类
     * @param invokeService 调用管理类
     * @param respTimeoutMills 响应超时时间
     * @param <T> 泛型
     * @param <R> 结果
     * @return 结果
     * @since 1.0.0
     */
    private <T extends MqCommonReq, R extends MqCommonResp> R callServer(Channel channel,
                                                                         T commonReq,
                                                                         Class<R> respClass,
                                                                         IInvokeService invokeService,
                                                                         long respTimeoutMills) {
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
            return JSON.parseObject(respJson, respClass);
        }
    }

}
