package com.github.houbb.mq.broker.utils;

import com.github.houbb.mq.broker.dto.BrokerServiceEntryChannel;
import com.github.houbb.mq.broker.dto.ServiceEntry;
import io.netty.channel.Channel;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class InnerChannelUtils {

    private InnerChannelUtils(){}

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
