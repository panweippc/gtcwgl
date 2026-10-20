package com.jxc.module.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jxc.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商实体
 *
 * @author jxc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 供应商分类ID */
    private Long categoryId;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 邮箱 */
    private String email;

    /** 地址 */
    private String address;

    /** 银行账号 */
    private String bankAccount;

    /** 开户行 */
    private String bankName;

    /** 等级:1-普通,2-优质,3-战略 */
    private Integer level;

    /** 状态:0-禁用,1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
