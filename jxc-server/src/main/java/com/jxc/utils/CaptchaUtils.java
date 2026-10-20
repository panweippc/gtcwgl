package com.jxc.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 * 使用 Java2D 生成 4 位字符 + 干扰线的验证码图片,输出 base64
 *
 * @author jxc
 */
public class CaptchaUtils {

    /** 图片宽度 */
    private static final int WIDTH = 120;
    /** 图片高度 */
    private static final int HEIGHT = 40;
    /** 验证码字符个数 */
    private static final int CODE_COUNT = 4;
    /** 干扰线数量 */
    private static final int LINE_COUNT = 20;
    /** 干扰点数量 */
    private static final int POINT_COUNT = 100;
    /** 验证码字符集(去掉易混淆的 0/O/1/l/I) */
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final Random RANDOM = new Random();

    /**
     * 生成验证码(图片 + 字符串)
     *
     * @return 包含验证码字符串和图片 base64 的数组,index 0=code,index 1=base64
     */
    public static String[] generate() {
        // 生成随机验证码字符串
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            codeBuilder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        String code = codeBuilder.toString();

        // 生成图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 画干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(randomColor(160, 220));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 画干扰点
        for (int i = 0; i < POINT_COUNT; i++) {
            g.setColor(randomColor(180, 230));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillOval(x, y, 1, 1);
        }

        // 画验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(20, 120));
            int x = 20 + i * 22;
            int y = 30 + RANDOM.nextInt(6);
            // 旋转角度 -15~15 度
            double theta = Math.toRadians(RANDOM.nextInt(30) - 15);
            g.rotate(theta, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x, y);
        }

        g.dispose();

        // 转 base64
        String base64;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            base64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }

        return new String[]{code, base64};
    }

    /**
     * 生成随机颜色
     *
     * @param min RGB 最小值
     * @param max RGB 最大值
     * @return 颜色
     */
    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }
}
