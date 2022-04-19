package com.github.houbb.mq.broker.support.valid;

import com.github.houbb.mq.broker.dto.BrokerRegisterReq;

/**
 * @author binbin.hou
 * @since 0.1.4
 */
public class BrokerRegisterValidService implements IBrokerRegisterValidService {

    @Override
    public boolean producerValid(BrokerRegisterReq registerReq) {
        return true;
    }

    @Override
    public boolean consumerValid(BrokerRegisterReq registerReq) {
        return true;
    }

}
