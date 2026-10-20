package com.jxc.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jxc.common.Result;
import com.jxc.common.ResultCode;
import com.jxc.context.UserContext;
import com.jxc.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 登录拦截器
 *
 * @author jxc
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 放行的路径关键字 */
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/captcha",
            "/error",
            "/doc.html",
            "/swagger",
            "/v2/api-docs",
            "/webjars",
            "/favicon.ico"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        // 白名单放行
        for (String path : WHITE_LIST) {
            if (uri.contains(path)) {
                return true;
            }
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return writeUnauthorized(response, ResultCode.UNAUTHORIZED.getMsg());
        }
        if (!jwtUtils.validateToken(token)) {
            return writeUnauthorized(response, ResultCode.UNAUTHORIZED.getMsg());
        }
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            return writeUnauthorized(response, ResultCode.UNAUTHORIZED.getMsg());
        }
        Object userIdObj = claims.get("userId");
        Long userId;
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else {
            userId = Long.parseLong(userIdObj.toString());
        }
        UserContext.setCurrentUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 写入未认证响应
     *
     * @param response 响应
     * @param msg      提示
     * @return false 拦截
     */
    private boolean writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.of(ResultCode.UNAUTHORIZED)));
        log.warn("未登录访问被拦截: {}", response);
        return false;
    }
}
