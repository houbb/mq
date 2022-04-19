package com.github.houbb.mq.common.support.status;

/**
 * 状态管理
 *
 * <p> project: rpc-StatusManager </p>
 * <p> create on 2019/10/30 20:48 </p>
 *
 * @author Administrator
 * @since 0.1.3
 */
public interface IStatusManager {

    /**
     * 获取状态编码
     * @return 状态编码
     * @since 0.1.3
     */
    boolean status();

    /**
     * 设置状态编码
     * @param status 编码
     * @return this
     * @since 0.1.3
     */
    IStatusManager status(final boolean status);

    /**
     * 初始化失败
     * @return 初始化失败
     * @since 0.1.4
     */
    boolean initFailed();

    /**
     * 设置初始化失败
     * @param failed 编码
     * @return this
     * @since 0.1.4
     */
    IStatusManager initFailed(final boolean failed);

}
