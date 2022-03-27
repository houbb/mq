package com.github.houbb.mq.common.util;

import com.alibaba.fastjson.JSON;
import com.github.houbb.mq.common.rpc.RpcMessageDto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class DelimiterUtil {

    private DelimiterUtil(){}

    /**
     * 分隔符
     */
    public static final String DELIMITER = "~!@#$%^&*";

    /**
     * 长度
     *
     * ps: 这个长度是必须的，避免把缓冲区打爆
     */
    public static final int LENGTH = 65535;

    /**
     * 分隔符 buffer
     * @since 1.0.0
     */
    public static final ByteBuf DELIMITER_BUF = Unpooled.copiedBuffer(DELIMITER.getBytes());

    /**
     * 获取对应的字节缓存
     * @param text 文本
     * @return 结果
     * @since 1.0.0
     */
    public static ByteBuf getByteBuf(String text) {
        return Unpooled.copiedBuffer(text.getBytes());
    }

    /**
     * 获取消息
     * @param rpcMessageDto 消息体
     * @return 结果
     * @since 1.0.0
     */
    public static ByteBuf getMessageDelimiterBuffer(RpcMessageDto rpcMessageDto) {
        String json = JSON.toJSONString(rpcMessageDto);
        String jsonDelimiter = json + DELIMITER;

        return Unpooled.copiedBuffer(jsonDelimiter.getBytes());
    }

}
