package com.github.houbb.mq.broker.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.broker.api.IMqBroker;
import com.github.houbb.mq.broker.api.IBrokerConsumerService;
import com.github.houbb.mq.broker.api.IBrokerProducerService;
import com.github.houbb.mq.broker.constant.BrokerConst;
import com.github.houbb.mq.broker.constant.BrokerRespCode;
import com.github.houbb.mq.broker.handler.MqBrokerHandler;
import com.github.houbb.mq.broker.support.api.LocalBrokerConsumerService;
import com.github.houbb.mq.broker.support.api.LocalBrokerProducerService;
import com.github.houbb.mq.broker.support.persist.IMqBrokerPersist;
import com.github.houbb.mq.broker.support.persist.LocalMqBrokerPersist;
import com.github.houbb.mq.broker.support.push.BrokerPushService;
import com.github.houbb.mq.broker.support.push.IBrokerPushService;
import com.github.houbb.mq.common.resp.MqException;
import com.github.houbb.mq.common.support.invoke.IInvokeService;
import com.github.houbb.mq.common.support.invoke.impl.InvokeService;
import com.github.houbb.mq.common.util.DelimiterUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqBroker extends Thread implements IMqBroker {

    private static final Log log = LogFactory.getLog(MqBroker.class);

    /**
     * 端口号
     */
    private final int port;

    /**
     * 处理类
     *
     * @since 0.0.3
     */
    private ChannelHandler channelHandler;

    /**
     * 调用管理类
     *
     * @since 1.0.0
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 消费者管理
     *
     * @since 0.0.3
     */
    private IBrokerConsumerService registerConsumerService = new LocalBrokerConsumerService();

    /**
     * 生产者管理
     *
     * @since 0.0.3
     */
    private IBrokerProducerService registerProducerService = new LocalBrokerProducerService();

    /**
     * 持久化类
     *
     * @since 0.0.3
     */
    private IMqBrokerPersist mqBrokerPersist = new LocalMqBrokerPersist();

    /**
     * 推送服务
     *
     * @since 0.0.3
     */
    private IBrokerPushService brokerPushService = new BrokerPushService();

    /**
     * 获取响应超时时间
     * @since 0.0.3
     */
    private long respTimeoutMills = 5000;

    public MqBroker() {
        this(BrokerConst.DEFAULT_PORT);
    }

    public MqBroker(int port) {
        this.port = port;
    }

    public void setRegisterConsumerService(IBrokerConsumerService registerConsumerService) {
        ArgUtil.notNull(registerConsumerService, "registerConsumerService");
        this.registerConsumerService = registerConsumerService;
    }

    public void setRegisterProducerService(IBrokerProducerService registerProducerService) {
        ArgUtil.notNull(registerProducerService, "registerProducerService");
        this.registerProducerService = registerProducerService;
    }

    public void setMqBrokerPersist(IMqBrokerPersist mqBrokerPersist) {
        ArgUtil.notNull(mqBrokerPersist, "mqBrokerPersist");

        this.mqBrokerPersist = mqBrokerPersist;
    }

    public void setBrokerPushService(IBrokerPushService brokerPushService) {
        ArgUtil.notNull(brokerPushService, "brokerPushService");

        this.brokerPushService = brokerPushService;
    }

    public void setRespTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
    }

    private ChannelHandler initChannelHandler() {
        MqBrokerHandler handler = new MqBrokerHandler();
        handler.setInvokeService(invokeService);
        handler.setRegisterConsumerService(registerConsumerService);
        handler.setRegisterProducerService(registerProducerService);
        handler.setMqBrokerPersist(mqBrokerPersist);
        handler.setBrokerPushService(brokerPushService);
        handler.setRespTimeoutMills(respTimeoutMills);

        return handler;
    }

    @Override
    public void run() {
        // 启动服务端
        log.info("MQ 中间人开始启动服务端 port: {}", port);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                    .addLast(initChannelHandler());
                        }
                    })
                    // 这个参数影响的是还没有被accept 取出的连接
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 这个参数只是过一段时间内客户端没有响应，服务端会发送一个 ack 包，以判断客户端是否还活着。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收进来的链接
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            log.info("MQ 中间人启动完成，监听【" + port + "】端口");

            channelFuture.channel().closeFuture().syncUninterruptibly();
            log.info("MQ 中间人关闭完成");
        } catch (Exception e) {
            log.error("MQ 中间人启动异常", e);
            throw new MqException(BrokerRespCode.RPC_INIT_FAILED);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
