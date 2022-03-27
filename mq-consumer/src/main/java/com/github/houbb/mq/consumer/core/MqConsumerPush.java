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
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
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
        log.info("MQ 消费者开始启动客户端 GROUP: {}, PORT: {}, brokerAddress: {}",
                groupName, port, brokerAddress);

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>(){
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new MqConsumerHandler());
                        }
                    })
                    .connect("localhost", port)
                    .syncUninterruptibly();

            log.info("MQ 消费者启动客户端完成，监听端口：" + port);
            channelFuture.channel().closeFuture().syncUninterruptibly();
            log.info("MQ 消费者开始客户端已关闭");
        } catch (Exception e) {
            log.error("MQ 消费者启动遇到异常", e);
            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }


    @Override
    public void subscribe(String topicName, String tagRegex) {

    }

    @Override
    public void registerListener(IMqConsumerListener listener) {

    }

}
