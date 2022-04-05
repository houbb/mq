package com.github.houbb.mq.common.support.invoke;


import com.github.houbb.mq.common.rpc.RpcMessageDto;

/**
 * 调用服务接口
 * @author binbin.hou
 * @since 1.0.0
 */
public interface IInvokeService {

    /**
     * 添加请求信息
     * @param seqId 序列号
     * @param timeoutMills 超时时间
     * @return this
     * @since 1.0.0
     */
    IInvokeService addRequest(final String seqId,
                              final long timeoutMills);

    /**
     * 放入结果
     * @param seqId 唯一标识
     * @param rpcResponse 响应结果
     * @return this
     * @since 1.0.0
     */
    IInvokeService addResponse(final String seqId, final RpcMessageDto rpcResponse);

    /**
     * 获取标志信息对应的结果
     * @param seqId 序列号
     * @return 结果
     * @since 1.0.0
     */
    RpcMessageDto getResponse(final String seqId);

    /**
     * 是否依然包含请求待处理
     * @return 是否
     * @since 0.0.5
     */
    boolean remainsRequest();

}
