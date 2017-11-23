package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by happyyangyuan at 2017/11/21
 */
@SpringBootApplication
@EnableDiscoveryClient
@RestController
@RefreshScope
public class ConfigReaderWithBusApplication0 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigReaderWithBusApplication0.class, args);
    }

    /**
     * Waning: {@link RefreshScope} does not support private properties
     */
    @Value("${message}")
    String message;

    @RequestMapping("/")
    public String home() {
        return message;
    }
}
