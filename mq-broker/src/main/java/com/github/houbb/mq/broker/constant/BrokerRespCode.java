package com.github.houbb.mq.broker.constant;

import com.github.houbb.heaven.response.respcode.RespCode;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public enum BrokerRespCode implements RespCode {

    RPC_INIT_FAILED("B00001", "中间人启动失败"),
    MSG_PUSH_FAILED("B00002", "中间人消息推送失败"),
            ;

    private final String code;
    private final String msg;

    BrokerRespCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
