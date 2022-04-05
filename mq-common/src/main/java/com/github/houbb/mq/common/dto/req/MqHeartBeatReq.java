package com.github.houbb.mq.common.dto.req;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqHeartBeatReq extends MqCommonReq {

    /**
     * address 信息
     * @since 0.0.6
     */
    private String address;

    /**
     * 端口号
     * @since 0.0.6
     */
    private int port;

    /**
     * 请求时间
     * @since 0.0.6
     */
    private long time;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
