package com.github.houbb.mq.consumer.core;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.exception.MqException;
import com.github.houbb.mq.consumer.api.IMqConsumer;
import com.github.houbb.mq.consumer.api.IMqConsumerListener;
import com.github.houbb.mq.consumer.constant.ConsumerConst;
import com.github.houbb.mq.consumer.constant.ConsumerRespCode;
import com.github.houbb.mq.consumer.handler.MqConsumerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

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
     * 端口号
     */
    private final int port;

    /**
     * 中间人地址
     */
    private String brokerAddress  = "";

    public MqConsumerPush(String groupName, int port) {
        this.groupName = groupName;
        this.port = port;
    }

    public MqConsumerPush(String groupName) {
        this(groupName, ConsumerConst.DEFAULT_PORT);
    }

    public MqConsumerPush() {
        this(ConsumerConst.DEFAULT_GROUP_NAME, ConsumerConst.DEFAULT_PORT);
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    @Override
    public void run() {
        // 启动服务端
        log.info("MQ 生产者开始启动服务端 groupName: {}, port: {}, brokerAddress: {}",
                groupName, port, brokerAddress);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new MqConsumerHandler());
                        }
                    })
                    // 这个参数影响的是还没有被accept 取出的连接
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 这个参数只是过一段时间内客户端没有响应，服务端会发送一个 ack 包，以判断客户端是否还活着。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收进来的链接
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            log.info("MQ 生产者启动完成，监听【" + port + "】端口");

            channelFuture.channel().closeFuture().syncUninterruptibly();
            log.info("MQ 生产者关闭完成");
        } catch (Exception e) {
            log.error("MQ 生产者启动异常", e);
            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }


    @Override
    public void subscribe(String topicName, String tagRegex) {

    }

    @Override
    public void registerListener(IMqConsumerListener listener) {

    }

}
