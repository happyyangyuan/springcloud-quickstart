package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by happyyangyuan at 2017/11/21
 */
@SpringBootApplication
@RestController
@EnableDiscoveryClient
@EnableFeignClients
public class ZipkinClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinClientApplication.class, args);
    }

    @Autowired
    private FeignServiceInterface feignService;

    @RequestMapping(value = "/")
    public String home() {
        System.out.println("ZipkinClientApplication        00000000000000000");
        return feignService.callServiceFeign("no name");
    }

    @FeignClient(value = "zipkin-client0")
    public interface FeignServiceInterface {
        @RequestMapping(value = "/", method = RequestMethod.GET)
        String callServiceFeign(@RequestParam(value = "name") String name);
    }

}
