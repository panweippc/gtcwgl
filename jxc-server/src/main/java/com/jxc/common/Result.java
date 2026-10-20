package com.jxc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结果封装
 *
 * @param <T> 数据泛型
 * @author jxc
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回(无数据)
     *
     * @param <T> 数据泛型
     * @return 结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回(带数据)
     *
     * @param data 数据
     * @param <T>  数据泛型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回(自定义提示)
     *
     * @param msg  提示
     * @param data 数据
     * @param <T>  数据泛型
     * @return 结果
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回
     *
     * @param msg 提示
     * @param <T> 数据泛型
     * @return 结果
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 失败返回(自定义状态码)
     *
     * @param code 状态码
     * @param msg  提示
     * @param <T>  数据泛型
     * @return 结果
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 根据状态码返回
     *
     * @param resultCode 状态码枚举
     * @param <T>        数据泛型
     * @return 结果
     */
    public static <T> Result<T> of(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }
}
