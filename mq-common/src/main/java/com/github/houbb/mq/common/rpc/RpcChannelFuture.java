package com.github.houbb.mq.common.rpc;

import io.netty.channel.ChannelFuture;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class RpcChannelFuture extends RpcAddress {

    /**
     * channel future 信息
     * @since 0.0.3
     */
    private ChannelFuture channelFuture;

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

}
