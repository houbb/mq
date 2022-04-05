package com.github.houbb.mq.consumer.constant;

import com.github.houbb.heaven.response.respcode.RespCode;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public enum ConsumerRespCode implements RespCode {

    RPC_INIT_FAILED("C00001", "消费者启动失败"),
    SUBSCRIBE_FAILED("C00002", "消费者注册失败"),
    UN_SUBSCRIBE_FAILED("C00003", "消费者注销失败"),
    ;

    private final String code;
    private final String msg;

    ConsumerRespCode(String code, String msg) {
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
