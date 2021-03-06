package com.zjtelcom.cpct.controller;

import com.zjtelcom.cpct.service.MqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqController extends BaseController implements ApplicationRunner {

    @Autowired
    private MqService mqService;

    @Override
    public void run(ApplicationArguments args) {
        mqService.initProducer();
        mqService.initConsumer();
    }

}
