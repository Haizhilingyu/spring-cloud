package com.md.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author md.cn
 * @version 2017/11/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseResult<T> implements Serializable {


    /**
     * 响应状态回执码
     */
    private Integer status;

    /**
     * 数据体
     */
    private T data;

    /**
     * 响应回执消息
     */
    private String msg;

    /**
     * 响应时间戳
     */
    private final long timestamps = System.currentTimeMillis();

    public synchronized static <T> ResponseResult<T> e(ResponseCode statusEnum) {
        return e(statusEnum,null);
    }

    public synchronized static <T> ResponseResult<T> e(ResponseCode statusEnum, T data) {
        ResponseResult<T> res = new ResponseResult<>();
        res.setStatus(statusEnum.code);
        res.setMsg(statusEnum.msg);
        res.setData(data);
        return res;
    }


    private static final long serialVersionUID = 8992436576262574064L;
}
