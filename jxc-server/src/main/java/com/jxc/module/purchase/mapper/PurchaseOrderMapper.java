package com.jxc.module.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.purchase.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购入库主表 Mapper
 *
 * @author jxc
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
