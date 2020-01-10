package com.md.common.util;

public enum NetworkEnum {

    SUCCESS("网卡修改成功", 1),
    FAIL("网卡修改失败", 0),
    FAIL_GATE("存在相同网关", 2),
    FAIL_IP("存在相同IP地址", 3);

    private int stateNum;

    private String description;

    NetworkEnum(String description, int stateNum) {
        this.stateNum = stateNum;
        this.description = description;
    }

    public int getStateNum() {
        return stateNum;
    }

    public String getDescription() {
        return description;
    }
}
