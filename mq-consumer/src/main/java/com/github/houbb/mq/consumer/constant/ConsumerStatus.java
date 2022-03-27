package com.github.houbb.mq.consumer.constant;

/**
 * 消费状态
 *
 * @author binbin.hou
 * @since 1.0.0
 */
public enum ConsumerStatus {
    SUCCESS("SUCCESS", "消费成功"),
    FAILED("FAILED", "消费失败"),
    CONSUMER_LATER("CONSUMER_LATER", "稍后消费"),
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
