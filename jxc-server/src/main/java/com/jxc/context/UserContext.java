package com.jxc.context;

import java.util.Objects;

/**
 * 当前登录用户上下文(ThreadLocal)
 *
 * @author jxc
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Long id = CURRENT_USER_ID.get();
        return Objects.isNull(id) ? null : id;
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
