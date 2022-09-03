package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.model.SeckillUser;

public interface UserService {

    SeckillUser findByphone(String phone);

    SeckillUser findAllByPhone(String phone);
}
