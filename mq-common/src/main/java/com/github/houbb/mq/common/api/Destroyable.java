package com.github.houbb.mq.common.api;

/**
 * 可销毁的
 * @author binbin.hou
 * @since 0.0.5
 */
public interface Destroyable {

    /**
     * 销毁方法
     * @since 0.0.8
     */
    void destroyAll();

}
