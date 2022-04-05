package com.github.houbb.mq.common.support.hook;

/**
 * rpc 关闭 hook
 * （1）可以添加对应的 hook 管理类
 * @since 0.0.5
 */
public interface RpcShutdownHook {

    /**
     * 钩子函数实现
     * @since 0.0.5
     */
    void hook();

}
