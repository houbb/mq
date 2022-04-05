package com.github.houbb.mq.common.rpc;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class RpcAddress {

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
}
