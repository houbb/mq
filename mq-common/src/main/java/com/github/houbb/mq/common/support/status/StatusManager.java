package com.github.houbb.mq.common.support.status;

/**
 * @author binbin.hou
 * @since 0.0.3
 */
public class StatusManager implements IStatusManager {

    private boolean status;

    @Override
    public boolean status() {
        return this.status;
    }

    @Override
    public IStatusManager status(boolean status) {
        this.status = status;

        return this;
    }

}
