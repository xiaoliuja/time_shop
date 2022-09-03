package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.SeckillSecondReq;

public interface LimitUserService {

    BaseResponse limitBucketUser(SeckillSecondReq req);

    BaseResponse orderV1(SeckillSecondReq req);


    BaseResponse createOrder(SeckillSecondReq req);

    //获取验证码
    BaseResponse<String> getVerifyDecode(SeckillSecondReq req);

    //隐藏秒杀下单
    BaseResponse orderHide(SeckillSecondReq.seckillGetVerify req);
}
