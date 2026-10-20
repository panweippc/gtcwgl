package com.jxc.utils;

import java.security.MessageDigest;

/**
 * 密码工具类(MD5 + 全局盐)
 *
 * @author jxc
 */
public class PasswordUtils {

    /** 全局盐 */
    private static final String SALT = "jxc2026";

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return md5(rawPassword + SALT);
    }

    /**
     * 校验密码
     *
     * @param rawPassword    原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }

    /**
     * MD5 计算
     *
     * @param text 文本
     * @return MD5
     */
    private static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
