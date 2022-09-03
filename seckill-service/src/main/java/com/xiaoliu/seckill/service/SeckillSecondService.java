package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.model.SeckillOrder;
import com.xiaoliu.seckill.model.SeckillProducts;


//秒杀
public interface SeckillSecondService {

    // 注：  BaseRequest封装一般只用于 controller层，业务层可以使用 BaseRsponse返回
    BaseResponse sOrder(SeckillSecondReq req);


    //悲观锁
    BaseResponse pOrder(SeckillSecondReq req);

    //乐观锁
    BaseResponse oOrder(SeckillSecondReq req) throws Exception;

    //redis+lua脚本
    BaseResponse luaOrder(SeckillSecondReq req) throws Exception;

    //redission
    BaseResponse redissonOrder(SeckillSecondReq req);

}
