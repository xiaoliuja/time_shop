package com.xiaoliu.seckill.service.impl;

import com.xiaoliu.seckill.dao.DemoDao;
import com.xiaoliu.seckill.model.Demo;
import com.xiaoliu.seckill.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {

    @Autowired
    private DemoDao demoDao;


    @Override
    public List<Demo> findAll() {
        Demo demo = new Demo();
        return demoDao.list(demo);
    }
}
