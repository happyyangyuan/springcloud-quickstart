package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by happyyangyuan at 2017/11/20
 */
@RestController
public class HiController {
    @Autowired
    private CallServiceHi hiServiceCaller;

    @RequestMapping("hi")
    public String hi(@RequestParam(required = false) String name) {
        return hiServiceCaller.sayHiFromClientOne(name);
    }
}
