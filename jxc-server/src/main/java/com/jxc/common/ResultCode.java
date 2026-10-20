package com.jxc.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统状态码枚举
 *
 * @author jxc
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 失败 */
    FAIL(500, "操作失败"),
    /** 未登录 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无权限 */
    FORBIDDEN(403, "无操作权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),
    /** 单据已审核不可修改 */
    ORDER_AUDITED(600, "单据已审核,不可修改或删除"),
    /** 库存不足 */
    STOCK_NOT_ENOUGH(601, "库存不足,禁止负库存出库");

    /** 状态码 */
    private final Integer code;

    /** 提示信息 */
    private final String msg;
}
