package com.github.houbb.mq.broker.dto.persist;

import com.github.houbb.mq.common.dto.req.MqConsumerPullReq;
import com.github.houbb.mq.common.dto.req.MqMessage;
import com.github.houbb.mq.common.rpc.RpcAddress;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class MqMessagePersistPull {

    /**
     * 消息体
     */
    private MqConsumerPullReq pullReq;

    /**
     * 地址信息
     */
    private RpcAddress rpcAddress;

    public MqConsumerPullReq getPullReq() {
        return pullReq;
    }

    public void setPullReq(MqConsumerPullReq pullReq) {
        this.pullReq = pullReq;
    }

    public RpcAddress getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(RpcAddress rpcAddress) {
        this.rpcAddress = rpcAddress;
    }
}
