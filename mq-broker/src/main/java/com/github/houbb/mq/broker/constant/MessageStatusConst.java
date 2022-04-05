package com.github.houbb.mq.broker.constant;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public final class MessageStatusConst {

    /**
     * 待消费
     */
    public static final String WAIT_CONSUMER = "W";

    /**
     * 消费中
     */
    public static final String PROCESS_CONSUMER = "P";

    /**
     * 消费完成
     */
    public static final String SUCCESS = "S";

    /**
     * 消费失败
     */
    public static final String FAILED = "F";

}
