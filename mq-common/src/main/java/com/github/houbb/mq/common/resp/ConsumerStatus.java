package com.github.houbb.mq.common.resp;

/**
 * 消费状态
 *
 * @author binbin.hou
 * @since 1.0.0
 */
public enum ConsumerStatus {
    SUCCESS("S", "消费成功"),
    FAILED("F", "消费失败"),
    CONSUMER_LATER("W", "稍后消费"),
    ;

    private final String code;
    private final String desc;

    ConsumerStatus(String code, String desc) {
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
