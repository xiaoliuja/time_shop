package com.xiaoliu.seckill.controller;

import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.model.Demo;
import com.xiaoliu.seckill.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/dem")
@Slf4j
public class DemoController {

    @Autowired
    private DemoService demoService;


    @RequestMapping("/list")
    public BaseResponse<List<Demo>> findAll(HttpSession session){

        return BaseResponse.OK(demoService.findAll());


    }
}
