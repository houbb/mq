package com.github.houbb.mq.common.resp;

import com.github.houbb.heaven.response.respcode.RespCode;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public enum MqCommonRespCode implements RespCode {

    SUCCESS("0000", "成功"),
    FAIL("9999", "失败"),
    TIMEOUT("8888", "超时"),

    RPC_GET_RESP_FAILED("10001", "RPC 获取响应失败")
    ;

    private final String code;
    private final String msg;

    MqCommonRespCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
