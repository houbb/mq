package com.github.houbb.mq.producer.core;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.mq.common.dto.MqMessage;
import com.github.houbb.mq.common.exception.MqException;
import com.github.houbb.mq.producer.api.IMqProducer;
import com.github.houbb.mq.producer.constant.ProducerConst;
import com.github.houbb.mq.producer.constant.ProducerRespCode;
import com.github.houbb.mq.producer.dto.SendResult;
import com.github.houbb.mq.producer.handler.MqProducerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
                            ch.pipeline().addLast(new MqProducerHandler());
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
            throw new MqException(ProducerRespCode.RPC_INIT_FAILED);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public SendResult send(MqMessage mqMessage) {
        return null;
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        return null;
    }

}
