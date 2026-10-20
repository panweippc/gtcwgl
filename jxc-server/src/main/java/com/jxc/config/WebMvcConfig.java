package com.jxc.config;

import com.jxc.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置:注册 JWT 拦截器、静态资源映射
 *
 * @author jxc
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /** 上传文件存储目录 */
    @Value("${jxc.upload.path}")
    private String uploadPath;

    /** 上传文件访问URL前缀 */
    @Value("${jxc.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/captcha",
                        "/error",
                        "/favicon.ico",
                        "/uploads/**"
                );
    }

    /**
     * 静态资源映射:将上传目录映射为可访问的URL路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将相对路径转为绝对路径,与文件保存逻辑保持一致(均基于JVM工作目录解析)
        java.io.File dir = new java.io.File(uploadPath).getAbsoluteFile();
        String location = dir.getPath().replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + location);
    }
}
