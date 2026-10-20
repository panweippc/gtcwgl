package com.jxc.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxc.common.BusinessException;
import com.jxc.common.PageResult;
import com.jxc.module.system.entity.SysRole;
import com.jxc.module.system.mapper.SysRoleMapper;
import com.jxc.module.system.mapper.SysRoleMenuMapper;
import com.jxc.module.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 *
 * @author jxc
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    public PageResult<SysRole> page(Long page, Long size, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByAsc(SysRole::getSort);
        IPage<SysRole> p = new Page<>(page, size);
        IPage<SysRole> result = page(p, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<SysRole> listAllEnabled() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSort);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // 先删后插
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                roleMenuMapper.insert(roleId, menuId);
            }
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public SysRole getRoleById(Long id) {
        return getById(id);
    }
}
