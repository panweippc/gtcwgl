package com.jxc.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author jxc
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jxc.jwt.secret}")
    private String secret;

    @Value("${jxc.jwt.expire}")
    private long expireMinutes;

    /**
     * 生成 token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("userId", userId);
        claims.put("username", username);
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + expireMinutes * 60 * 1000L);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析 token
     *
     * @param token token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 token 是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }

    /**
     * 从 token 中获取用户ID
     *
     * @param token token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }
        return null;
    }
}
