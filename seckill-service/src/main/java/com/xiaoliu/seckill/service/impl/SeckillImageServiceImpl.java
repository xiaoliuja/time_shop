package com.xiaoliu.seckill.service.impl;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.RateLimiter;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.Constant;
import com.xiaoliu.seckill.base.SeckillReqV3;
import com.xiaoliu.seckill.dao.SeckillOrderDao;
import com.xiaoliu.seckill.dao.SeckillProductsDao;
import com.xiaoliu.seckill.dao.SeckillUserDao;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillOrder;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.SeckillImageService;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 图片验证码验证逻辑以及下单
 */
@Service
@Slf4j
public class SeckillImageServiceImpl implements SeckillImageService {

    @Autowired
    private SeckillProductsDao seckillProductsDao;
    @Autowired
    private SeckillUserDao seckillUserDao;
    @Autowired
    private SeckillOrderDao seckillOrderDao;
    @Autowired
    private RedisUtil redisUtil;


    RateLimiter rateLimiter = RateLimiter.create(5);

    //图片验证码 验证接口
    @Override
    public BaseResponse orderV3(SeckillReqV3 seckillReqV3) {
        log.info("===[开始调用下单接口应用限流]===");

        log.info("===[开始校验用户信息、商品信息、库存信息、秒杀开始时间、重复下单、图片验证码信息]===");
        BaseResponse baseResponse = validParamImage(seckillReqV3.getProductId(), seckillReqV3.getUserId(), seckillReqV3.getImageId(), seckillReqV3.getImageCode());
        if (baseResponse.getCode() != 0){
            return baseResponse;
        }


        // 非阻塞 限流
        if (!rateLimiter.tryAcquire()){
            log.error("===[该请求被限流了，返回失败！]===");
            return BaseResponse.error(ErrorMessage.LIMIT_USER_MESSAGE);
        }


        log.info("===[限流通过！！！！]====");




        log.info("===[开始一人多单校验]===");
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(seckillReqV3.getUserId());
        seckillOrder.setProductId(seckillReqV3.getProductId());
        int count = seckillOrderDao.count(seckillOrder);
        if (count>0){
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }
        log.info("通过一人多单校验");

        //生成订单
        SeckillOrder seckillOrder1 = new SeckillOrder();
        seckillOrder1.setProductId(seckillReqV3.getProductId());
        seckillOrder1.setUserId(seckillReqV3.getUserId());
        seckillOrder1.setCreateTime(new Date());
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(seckillReqV3.getProductId());
        seckillOrder1.setProductName(seckillProducts.getName());
        seckillOrderDao.insert(seckillOrder1);



        seckillProducts.setSaled(seckillProducts.getSaled()+1);
        seckillProducts.setUpdatedTime(new Date());
        seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);

        return BaseResponse.ok(true);
    }


    private BaseResponse validParamImage(Long productId,Long userId,String imageId,String imageCode){
        //从头学到尾巴 所有的最全的逻辑判断。。。
        SeckillProducts products = seckillProductsDao.selectByPrimaryKey(productId);
        if (products == null){
            log.error("===[产品信息不存在]===");
            return BaseResponse.error(ErrorMessage.PRODUCT_ERROR);
        }
        SeckillUser user = seckillUserDao.selectByPrimaryKey(userId);
        if (user == null){
            log.error("===[用户不存在]===");
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }
        if (products.getStartBuyTime().getTime() > System.currentTimeMillis()){
            log.error("===[秒杀还未开始]===");
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }
        //最基本的判断逻辑
        if (products.getSaled() >= products.getCount()){
            log.error("===[]===库存不足");
            return BaseResponse.error(ErrorMessage.COUNT_NOT_ENOUGH);
        }
        if (seckillOrderDao.countForUpdate(productId, userId) > 0){
            log.error("===[用户重复下单！]===");
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }

        //校验图形验证码  组装key
        String key = String.format(Constant.rediskey.SECKILL_IMAGE_CODE, imageId);
        if ( !redisUtil.hasKey(key) || !Objects.equal(redisUtil.get(key), imageCode)){
            //如果redis内部没有获取到该 key或者 调用图片验证码接口传的key在redis内部的value验证码与用户输入的验证码不相等
            log.error("===[图片校验未通过]===");
            return BaseResponse.error(ErrorMessage.IMAGE_code_ERROR);
        }
        return BaseResponse.ok(true);
    }
}
