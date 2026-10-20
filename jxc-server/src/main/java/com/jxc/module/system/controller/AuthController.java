package com.jxc.module.system.controller;

import com.jxc.common.BusinessException;
import com.jxc.common.Result;
import com.jxc.context.UserContext;
import com.jxc.module.system.dto.LoginDTO;
import com.jxc.module.system.service.AuthService;
import com.jxc.module.system.vo.LoginVO;
import com.jxc.module.system.vo.UserInfoVO;
import com.jxc.utils.CaptchaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证控制器
 * 提供验证码生成、登录、用户信息、退出等接口
 *
 * @author jxc
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 验证码缓存
     * key=captchaKey, value={code: 验证码字符串, expireAt: 过期时间戳(毫秒)}
     * 单机场景使用内存 Map,生产环境建议用 Redis
     */
    private static final Map<String, Map<String, Object>> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    /** 验证码有效期 5 分钟 */
    private static final long CAPTCHA_EXPIRE_MS = 5 * 60 * 1000L;

    /**
     * 获取验证码
     *
     * @return captchaKey-验证码唯一标识,captchaImg-base64 图片
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        String[] arr = CaptchaUtils.generate();
        String code = arr[0];
        String base64 = arr[1];
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        // 存入缓存
        Map<String, Object> value = new HashMap<>(4);
        value.put("code", code);
        value.put("expireAt", System.currentTimeMillis() + CAPTCHA_EXPIRE_MS);
        CAPTCHA_CACHE.put(captchaKey, value);
        Map<String, String> result = new HashMap<>(4);
        result.put("captchaKey", captchaKey);
        result.put("captchaImg", base64);
        return Result.success(result);
    }

    /**
     * 校验验证码(校验后立即清除,防止重复使用)
     *
     * @param captchaKey  验证码 key
     * @param captchaCode 用户输入的验证码
     */
    public static void verifyCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            throw new BusinessException("请输入验证码");
        }
        Map<String, Object> cached = CAPTCHA_CACHE.remove(captchaKey);
        if (cached == null) {
            throw new BusinessException("验证码已过期,请刷新后重试");
        }
        Long expireAt = (Long) cached.get("expireAt");
        if (System.currentTimeMillis() > expireAt) {
            throw new BusinessException("验证码已过期,请刷新后重试");
        }
        String realCode = (String) cached.get("code");
        if (!realCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("验证码不正确");
        }
    }

    /**
     * 登录
     *
     * @param dto 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        return Result.success(authService.login(dto));
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(authService.getUserInfo(userId));
    }

    /**
     * 退出登录(前端清除 token 即可)
     *
     * @return 结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
