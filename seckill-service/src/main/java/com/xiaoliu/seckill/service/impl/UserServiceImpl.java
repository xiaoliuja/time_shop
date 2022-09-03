package com.xiaoliu.seckill.service.impl;

import com.xiaoliu.seckill.dao.SeckillUserDao;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SeckillUserDao seckillUserDao;

    @Override
    public SeckillUser findByphone(String phone) {
        return seckillUserDao.selectByPhone(phone);
    }

    @Override
    public SeckillUser findAllByPhone(String phone) {
        return seckillUserDao.findAllByPhone(phone);
    }



}
