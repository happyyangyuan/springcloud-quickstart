package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
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
public class ZipkinClientApplication0 {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinClientApplication0.class, args);
    }

    @Autowired
    ZipkinClientService1Interface zipkinClientService1;

    @RequestMapping("/")
    String home(@RequestParam("name") String name) {
        return zipkinClientService1.call(name);
    }

    @FeignClient("zipkin-client1")
    interface ZipkinClientService1Interface {
        @RequestMapping("/")
        String call(@RequestParam("name") String name);
    }

}
