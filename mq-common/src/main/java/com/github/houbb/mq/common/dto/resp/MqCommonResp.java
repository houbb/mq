package com.github.houbb.mq.common.dto.resp;

import java.io.Serializable;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class MqCommonResp implements Serializable {

    /**
     * 响应编码
     * @since 1.0.0
     */
    private String respCode;

    /**
     * 响应消息
     * @since 1.0.0
     */
    private String respMessage;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMessage() {
        return respMessage;
    }

    public void setRespMessage(String respMessage) {
        this.respMessage = respMessage;
    }
}
