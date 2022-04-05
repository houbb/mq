package com.github.houbb.mq.broker.dto;

import io.netty.channel.Channel;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class BrokerServiceEntryChannel extends ServiceEntry {

    private Channel channel;

    /**
     * 最后访问时间
     * @since 0.0.6
     */
    private long lastAccessTime;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
