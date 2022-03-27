package com.github.houbb.mq.producer.constant;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public enum SendStatus {
    S("S", "成功"),
    F("F", "失败"),
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
