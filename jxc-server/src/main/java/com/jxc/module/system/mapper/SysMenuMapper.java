package com.jxc.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxc.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper
 *
 * @author jxc
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据ID集合查询启用的菜单
     *
     * @param ids ID集合
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByIds(@org.apache.ibatis.annotations.Param("ids") List<Long> ids);
}
