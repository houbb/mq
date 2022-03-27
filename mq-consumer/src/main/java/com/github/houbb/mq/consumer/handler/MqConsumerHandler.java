package com.github.houbb.mq.consumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqConsumerHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        //nothing
    }

}
