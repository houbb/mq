package com.github.houbb.mq.broker.dto;

import com.github.houbb.mq.common.rpc.RpcAddress;

/**
 * @author binbin.hou
 * @since 1.0.0
 */
public class ServiceEntry extends RpcAddress {

    /**
     * 分组名称
     */
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "groupName='" + groupName + '\'' +
                "} " + super.toString();
    }

}
