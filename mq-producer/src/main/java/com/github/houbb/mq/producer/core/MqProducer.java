package com.github.houbb.mq.producer.core;

import com.alibaba.fastjson.JSON;
import com.github.houbb.id.core.util.IdHelper;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.constant.MethodType;
import com.github.houbb.mq.common.dto.req.MqCommonReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.dto.resp.MqCommonResp;
import com.github.houbb.mq.common.exception.MqCommonRespCode;
import com.github.houbb.mq.common.exception.MqException;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.invoke.impl.InvokeService;
import com.github.houbb.mq.common.util.ChannelUtil;
import com.github.houbb.mq.common.util.DelimiterUtil;
import com.github.houbb.mq.producer.api.IMqProducer;
import com.github.houbb.mq.producer.constant.ProducerConst;
import com.github.houbb.mq.producer.constant.ProducerRespCode;
import com.github.houbb.mq.producer.constant.SendStatus;
import com.github.houbb.mq.producer.dto.SendResult;
import com.github.houbb.mq.producer.handler.MqProducerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

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
    private final String groupName;

    /**
     * 端口号
     */
    private final int port;

    /**
     * 中间人地址
     */
    private String brokerAddress  = "";

    /**
     * channel 信息
     * @since 0.0.2
     */
    private ChannelFuture channelFuture;

    /**
     * 客户端处理 handler
     * @since 0.0.2
     */
    private ChannelHandler channelHandler;

    /**
     * 调用管理服务
     * @since 0.0.2
     */
    private final IInvokeService invokeService = new InvokeService();

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
     * 粘包处理分隔符
     * @since 1.0.0
     */
    private String delimiter = DelimiterUtil.DELIMITER;


    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public MqProducer(String groupName, int port) {
        this.groupName = groupName;
        this.port = port;
    }

    public MqProducer(String groupName) {
        this(groupName, ProducerConst.DEFAULT_PORT);
    }

    public MqProducer() {
        this(ProducerConst.DEFAULT_GROUP_NAME, ProducerConst.DEFAULT_PORT);
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public void setRespTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
    }

    public boolean isEnableFlag() {
        return enableFlag;
    }

    private void initChannelHandler() {
        final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(delimiter);

        final MqProducerHandler mqProducerHandler = new MqProducerHandler();
        mqProducerHandler.setInvokeService(invokeService);

        // handler 实际上会被多次调用，如果不是 @Shareable，应该每次都重新创建。
        ChannelHandler handler = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                        .addLast(mqProducerHandler);
            }
        };

        this.channelHandler = handler;
    }

    @Override
    public synchronized void run() {
        // 启动服务端
        log.info("MQ 生产者开始启动客户端 GROUP: {}, PORT: {}, brokerAddress: {}",
                groupName, port, brokerAddress);

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // channel handler
            this.initChannelHandler();

            Bootstrap bootstrap = new Bootstrap();
            channelFuture = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>(){
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(channelHandler);
                        }
                    })
                    .connect("localhost", port)
                    .syncUninterruptibly();

            log.info("MQ 生产者启动客户端完成，监听端口：" + port);

            // 标识为可用
            enableFlag = true;
        } catch (Exception e) {
            log.error("MQ 生产者启动遇到异常", e);
            throw new MqException(ProducerRespCode.RPC_INIT_FAILED);
        }
    }

    @Override
    public SendResult send(MqMessage mqMessage) {
        String messageId = IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MESSAGE);

        MqCommonResp resp = callServer(mqMessage, MqCommonResp.class);
        if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())) {
            return SendResult.of(messageId, SendStatus.SUCCESS);
        }

        return SendResult.of(messageId, SendStatus.FAILED);
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        String messageId = IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MESSAGE);

        this.callServer(mqMessage, null);

        return SendResult.of(messageId, SendStatus.SUCCESS);
    }

    /**
     * 调用服务端
     * @param commonReq 通用请求
     * @param respClass 类
     * @param <T> 泛型
     * @param <R> 结果
     * @return 结果
     * @since 1.0.0
     */
    public <T extends MqCommonReq, R extends MqCommonResp> R callServer(T commonReq, Class<R> respClass) {
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
        Channel channel = channelFuture.channel();
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
