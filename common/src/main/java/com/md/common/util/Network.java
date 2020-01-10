package com.md.common.util;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by hai on 4/6/17.
 * 网卡对象
 * 用于存储网卡信息
 */
@Data
@Accessors(chain = true)
public class Network {

    /**
     * 网卡名称
     */
    private String name;

    /**
     * IP地址（IPV4）
     */
    private String ipv4;

    /**
     * IP地址（IPV6）
     */
    private String ipv6;

    /**
     * Mac地址
     */
    private String mac;

    /**
     * 子网掩码
     */
    private String code;

    /**
     * 网关
     */
    private String gate;

    /**
     * 是否为管理网卡（1为管理网卡，0为测试网卡），本系统未启用
     */
    private int manager = 0;
}
