package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by happyyangyuan at 2017/11/21
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@RestController
public class ZipkinClientApplication1 {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinClientApplication1.class, args);
    }

    @RequestMapping("/")
    String home(@RequestParam("name") String name) {
        System.out.println("00000000000000000         " + name);
        return "hello " + name;
    }

}
