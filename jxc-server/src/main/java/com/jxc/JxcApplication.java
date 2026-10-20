package com.jxc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 进销存管理系统启动类
 *
 * @author jxc
 */
@SpringBootApplication
@MapperScan("com.jxc.module.**.mapper")
@EnableTransactionManagement
@EnableAsync
public class JxcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JxcApplication.class, args);
        System.out.println("====================================");
        System.out.println("  进销存管理系统后端启动成功");
        System.out.println("  访问地址: http://localhost:8080/jxc");
        System.out.println("====================================");
    }
}
