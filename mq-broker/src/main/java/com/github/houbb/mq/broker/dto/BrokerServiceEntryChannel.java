package com.github.houbb.mq.broker.dto;

import io.netty.channel.Channel;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class BrokerServiceEntryChannel extends ServiceEntry {

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
