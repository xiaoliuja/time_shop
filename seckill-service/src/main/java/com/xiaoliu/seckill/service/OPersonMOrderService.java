package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.SeckillSecondReq;

public interface OPersonMOrderService {

    BaseResponse omOrder(SeckillSecondReq req);

    BaseResponse smOrder(SeckillSecondReq req);

    BaseResponse stmOrder(SeckillSecondReq req);

    BaseResponse redisOnlyOrder(SeckillSecondReq req) throws Exception;
}
