package com.github.houbb.mq.common.rpc;

import com.github.houbb.load.balance.support.server.IServer;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class RpcAddress implements IServer {

    /**
     * address 信息
     * @since 0.0.3
     */
    private String address;

    /**
     * 端口号
     * @since 0.0.3
     */
    private int port;

    /**
     * 权重
     * @since 0.0.3
     */
    private int weight;

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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String url() {
        return this.address+":"+port;
    }

    @Override
    public int weight() {
        return this.weight;
    }
}
