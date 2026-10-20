package com.jxc.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author jxc
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误状态码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
