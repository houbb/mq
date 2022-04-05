package com.github.houbb.mq.broker.utils;

import com.github.houbb.mq.broker.dto.BrokerServiceEntryChannel;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import com.github.houbb.mq.common.rpc.RpcChannelFuture;
import io.netty.channel.Channel;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class InnerChannelUtils {

    private InnerChannelUtils(){}

    /**
     * 构建基本服务地址
     * @param rpcChannelFuture 信息
     * @return 结果
     * @since 0.0.5
     */
    public static ServiceEntry buildServiceEntry(RpcChannelFuture rpcChannelFuture) {
        ServiceEntry serviceEntry = new ServiceEntry();

        serviceEntry.setAddress(rpcChannelFuture.getAddress());
        serviceEntry.setPort(rpcChannelFuture.getPort());
        serviceEntry.setWeight(rpcChannelFuture.getWeight());
        return serviceEntry;
    }

    public static BrokerServiceEntryChannel buildEntryChannel(ServiceEntry serviceEntry,
                                                              Channel channel) {
        BrokerServiceEntryChannel result = new BrokerServiceEntryChannel();
        result.setChannel(channel);
        result.setGroupName(serviceEntry.getGroupName());
        result.setAddress(serviceEntry.getAddress());
        result.setPort(serviceEntry.getPort());
        result.setWeight(serviceEntry.getWeight());
        return result;
    }

}
