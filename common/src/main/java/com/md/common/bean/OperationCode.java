package com.md.common.bean;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 数据操作代码
 * @author mac
 */
@NoArgsConstructor
@AllArgsConstructor
public enum OperationCode {
    /**
     * 成功标识
     */
    SUCCESS(1,"操作成功"),
    /**
     * 失败标识
     */
    FAIL(0, "操作失败");

    public Integer code;

    public String msg;
}
