package com.github.houbb.mq.producer.constant;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public enum SendStatus {
    SUCCESS("SUCCESS", "发送成功"),
    FAILED("FAILED", "发送失败"),
    ;

    private final String code;
    private final String desc;

    SendStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
