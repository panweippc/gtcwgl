package com.jxc.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.jxc.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * MyBatis-Plus 配置
 *
 * @author jxc
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页与乐观锁拦截器
     *
     * @return 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        pageInterceptor.setMaxLimit(500L);
        pageInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(pageInterceptor);
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充处理器:创建/更新时间、操作人
     *
     * @return 处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                Date now = new Date();
                Long currentUserId = UserContext.getCurrentUserId();
                this.strictInsertFill(metaObject, "createTime", Date.class, now);
                this.strictInsertFill(metaObject, "updateTime", Date.class, now);
                if (currentUserId != null) {
                    this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
                    this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
                Long currentUserId = UserContext.getCurrentUserId();
                if (currentUserId != null) {
                    this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
                }
            }
        };
    }
}
