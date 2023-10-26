package com.baili.sharingPlatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author baili
 * @date 2022年05月10日4:06 下午
 */
@SpringBootApplication(scanBasePackages = "com.baili.sharingPlatform")
public class SharingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(SharingPlatformApplication.class).build();
        app.run(args);
    }
}
