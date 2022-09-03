package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.SeckillReqV3;

//抢购结合图形验证码
public interface SeckillImageService {
    BaseResponse orderV3(SeckillReqV3 seckillReqV3);
}
