package com.jxc.module.system.controller;

import com.jxc.common.BusinessException;
import com.jxc.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author jxc
 */
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class FileController {

    /** 上传文件存储目录 */
    @Value("${jxc.upload.path}")
    private String uploadPath;

    /** 上传文件访问URL前缀 */
    @Value("${jxc.upload.url-prefix}")
    private String urlPrefix;

    /** 上下文路径 */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 上传图片文件
     *
     * @param file 上传的文件
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("文件格式不正确");
        }
        // 校验文件后缀(仅支持图片)
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        List<String> allowedSuffixes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
        if (!allowedSuffixes.contains(suffix)) {
            throw new BusinessException("仅支持图片格式: jpg/jpeg/png/gif/bmp");
        }
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        // 将相对路径转为绝对路径,避免 transferTo 将相对路径解析到 Tomcat 临时目录导致找不到路径
        File dir = new File(uploadPath).getAbsoluteFile();
        // 创建目录
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败: " + dir.getPath());
        }
        File dest = new File(dir, fileName);
        // 保存文件
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
        // 返回完整访问URL(含context-path)
        String url = contextPath + urlPrefix + "/" + fileName;
        return Result.success(url);
    }
}
