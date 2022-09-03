package com.xiaoliu.seckill.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.google.common.util.concurrent.RateLimiter;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.Constant;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.config.DistrubuteLimit;
import com.xiaoliu.seckill.dao.SeckillOrderDao;
import com.xiaoliu.seckill.dao.SeckillProductsDao;
import com.xiaoliu.seckill.dao.SeckillUserDao;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillOrder;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.LimitUserService;
import com.xiaoliu.seckill.service.UserService;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class limitUserServiceImpl implements LimitUserService {


    @Autowired
    private SeckillProductsDao seckillProductsDao;
    @Autowired
    private SeckillOrderDao seckillOrderDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DistrubuteLimit distrubuteLimit;
    @Autowired
    private SeckillUserDao seckillUserDao;


    //工具类Ratelimiter
    RateLimiter rateLimiter = RateLimiter.create(10);



    /**
     * 令牌桶实现限流
     * Google开源的Java工具类 Guava 内部提供了限流工具类 RateLimiter， 内部实现了令牌桶算法
     * 后面的代码逻辑 就把前面的 一人多单的悲观锁实现给粘贴过来了，不重要，重要的是前面的限流
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse limitBucketUser(SeckillSecondReq req) {

        log.info("===[开始调用限流下单]===");

        log.info("===[开始限流程序]====");
        //阻塞式限流 获取令牌
        /*double acquire = rateLimiter.acquire();
        log.info("令牌桶限流等待时间{}",acquire);*/

        // 非阻塞 限流
        if (!rateLimiter.tryAcquire()){
            log.error("===[该请求被限流了，返回失败！]===");
            return BaseResponse.error(ErrorMessage.LIMIT_USER_MESSAGE);
        }


        log.info("===[限流通过！！！！]====");




        log.info("===[开始三层逻辑校验]===");
            BaseResponse baseResponse = validateParamForUpdate(req.getProductId(), req.getUserId());
            if (baseResponse.getCode() != 0){
                return baseResponse;
            }
            log.info("===[逻辑校验通过]===");


            log.info("===[开始一人多单校验]===");
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setUserId(req.getUserId());
            seckillOrder.setProductId(req.getProductId());
            int count = seckillOrderDao.count(seckillOrder);
            if (count>0){
                return BaseResponse.error(ErrorMessage.MANY_ORDER);
            }
            log.info("通过一人多单校验");


            //生成订单
            SeckillOrder seckillOrder1 = new SeckillOrder();
            seckillOrder1.setProductId(req.getProductId());
            seckillOrder1.setUserId(req.getUserId());
            seckillOrder1.setCreateTime(new Date());
            SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
            seckillOrder1.setProductName(seckillProducts.getName());
            seckillOrderDao.insert(seckillOrder1);

            //扣减库存   多并发下扣减库存的详细操作前面已经讲过了，这里就不在详细的写了
            // 就写为普通的扣减库存了
            seckillProducts.setSaled(seckillProducts.getSaled()+1);
            seckillProducts.setUpdatedTime(new Date());
            seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);


            return BaseResponse.ok(true);
    }


    //redis+lua
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse orderV1(SeckillSecondReq req) {
        log.info("===[开始调用下单接口（应用限流）]===");

        log.info("===[开始经过限流程序]===");
        //分布式限流

        if (!distrubuteLimit.exec()){
            log.info("你被分布式限流了！直接返回失败！");
            return BaseResponse.error(ErrorMessage.LIMIT_USER_MESSAGE);
        }
        log.info("===[限流程序通过！]===");




        log.info("===[开始三层逻辑校验]===");
        BaseResponse baseResponse = validateParamForUpdate(req.getProductId(), req.getUserId());
        if (baseResponse.getCode() != 0){
            return baseResponse;
        }
        log.info("===[逻辑校验通过]===");


        log.info("===[开始一人多单校验]===");
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(req.getUserId());
        seckillOrder.setProductId(req.getProductId());
        int count = seckillOrderDao.count(seckillOrder);
        if (count>0){
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }
        log.info("通过一人多单校验");

        //生成订单
        SeckillOrder seckillOrder1 = new SeckillOrder();
        seckillOrder1.setProductId(req.getProductId());
        seckillOrder1.setUserId(req.getUserId());
        seckillOrder1.setCreateTime(new Date());
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
        seckillOrder1.setProductName(seckillProducts.getName());
        seckillOrderDao.insert(seckillOrder1);

        //扣减库存   多并发下扣减库存的详细操作前面已经讲过了，这里就不在详细的写了
        // 就写为普通的扣减库存了
        seckillProducts.setSaled(seckillProducts.getSaled()+1);
        seckillProducts.setUpdatedTime(new Date());
        seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);

        return BaseResponse.ok(true);
    }

    //普通的创建订单扣减库存
    @Override
    public BaseResponse createOrder(SeckillSecondReq req) {
        log.info("===[开始创建订单]===");
        //生成订单
        SeckillOrder seckillOrder1 = new SeckillOrder();
        seckillOrder1.setProductId(req.getProductId());
        seckillOrder1.setUserId(req.getUserId());
        seckillOrder1.setCreateTime(new Date());
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
        seckillOrder1.setProductName(seckillProducts.getName());
        seckillOrderDao.insert(seckillOrder1);

        log.info("===[创建订单成功]===");


       //扣减库存 update ...  saled=saled+1
        seckillProductsDao.updtaeSaled(req.getProductId());

        return BaseResponse.ok(true);
    }

    @Override
    public BaseResponse<String> getVerifyDecode(SeckillSecondReq req) {
        log.info("====[开始调用获取验证码的接口]====");

        //这个接口 主要是要用 这个里面 秒杀未开始的判断   其他的其实为秒杀的判断其实按照逻辑来说也应该有，
        BaseResponse baseResponse = validateParamForUpdate(req.getProductId(), req.getUserId());
        if (baseResponse.getCode() != 0){
            return baseResponse;
        }

        //拼接一个redis的key   "sk:limit:%s:%s"
        String key = String.format(Constant.rediskey.VERIFY_DECODE_KEY,req.getProductId(),req.getUserId());
        //value 为一个加密的验证码  valueVerify这样设置，会使得更难以破解，更加动态化，每个key都很不一样，加密出来的安全性更高
        /**
         *  如果只用这个 用户id和商品id进行md5的加密的话其实是很不安全的，尤其是这种自增的id，如果写脚本的知道我们的加密方式其实是很好破解的；
         *  所以我们在前面加上一段 相当于一段常量，写死在代码中只要用猜不到，那就破解不了，为了更安全我们在将系统的毫秒时间加进去，让这个k随时都为一个变化的key，他们就更猜不到了
         */

        String valueVerify = Constant.rediskey.VERIFY_DECOED_SALT + req.getUserId() + req.getProductId() + System.currentTimeMillis();
        String md5Hex = DigestUtils.md5Hex(valueVerify);
        redisUtil.set(key,md5Hex, 60);

        log.info("===[加密完成]====");
        return BaseResponse.ok(md5Hex);
    }





    @Override
    public BaseResponse orderHide(SeckillSecondReq.seckillGetVerify req) {
        log.info("===[隐藏url接口的秒杀下单逻辑校验开始]===");


        log.info("===[开始经过限流程序]===");

        // 非阻塞 限流
        if (!rateLimiter.tryAcquire()){
            log.error("===[该请求被限流了，返回失败！]===");
            return BaseResponse.error(ErrorMessage.LIMIT_USER_MESSAGE);
        }


        log.info("===[限流通过！！！！]====");


        log.info("开始通过逻辑校验");
        BaseResponse baseResponse = validateParamForVerify(req.getProductId(), req.getUserId(), req.getDecode());
        if (baseResponse.getCode() != 0){
            return baseResponse;
        }



        log.info("===[开始一人多单校验]===");
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(req.getUserId());
        seckillOrder.setProductId(req.getProductId());
        int count = seckillOrderDao.count(seckillOrder);
        if (count>0){
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }
        log.info("通过一人多单校验");

        //生成订单
        SeckillOrder seckillOrder1 = new SeckillOrder();
        seckillOrder1.setProductId(req.getProductId());
        seckillOrder1.setUserId(req.getUserId());
        seckillOrder1.setCreateTime(new Date());
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
        seckillOrder1.setProductName(seckillProducts.getName());
        seckillOrderDao.insert(seckillOrder1);



        seckillProducts.setSaled(seckillProducts.getSaled()+1);
        seckillProducts.setUpdatedTime(new Date());
        seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);

        return BaseResponse.ok(true);
    }






    private BaseResponse validateParamForVerify(Long productId,Long userId,String decode){

        //限制用户访问的频次
        //以前没注意，其实这个key的值也有一定的说法，你比如这个来说，你限制的是每个用户的频次，所以说对于每个用户抢购每件商品来说都有一个唯一的key，而且对统一用户来说这个key的值是不会变化的，所以用一个常量+productid和userid
        String key1 =  String.format(Constant.rediskey.SECKILL_USER_COUNT, productId,userId);
        //不能时间过长，你想，如果时间过长的话，那么用户可能好几天，只能访问6次。。这台扯了，应该控制寄几秒之内才算合里，防止有人调用接口不断的刷请求。
        /*redisUtil.set(key1,0, 60*60);*/
        //value的值增加以后，会返回value的值
        long count = redisUtil.incr(key1,1L);
        redisUtil.expire(key1, 3);
        if (count > 1){
            log.error("===[你访问的频次过于频繁达到上限]===");
            return BaseResponse.error(ErrorMessage.USER_COUNT_TOO_MANY);
        }


        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(productId);

        if (seckillProducts == null){
            return BaseResponse.error(ErrorMessage.PRODUCT_ERROR);
        }

        SeckillUser user = seckillUserDao.selectByPrimaryKey(userId);
        if (user == null){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }
        if (seckillProducts.getStartBuyTime().getTime() > System.currentTimeMillis()){
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }
        if (seckillOrderDao.countForUpdate(productId,userId ) > 0){
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }



        //验证码的校验 校验通过执行真实下单的接口逻辑
        String key = String.format(Constant.rediskey.VERIFY_DECODE_KEY,productId,userId);
        if (!decode.equals(String.valueOf(redisUtil.get(key)))){
            return BaseResponse.error(ErrorMessage.ERROR_DECODE);
        }


        return new BaseResponse(0, "判断通过！！！", null);
    }






    //悲观锁的三层逻辑校验
    private BaseResponse validateParamForUpdate(Long productId,Long userId){
        //加forUpdate  开始的逻 辑校验  sleect * from seckil_products  where id =  **  for update。
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKeyForUpdate(productId);

        if (seckillProducts == null){
            log.error("---产品不存在---");
            return BaseResponse.error(ErrorMessage.PRODUCT_ERROR);
        }

        if (seckillProducts.getStartBuyTime().getTime() > System.currentTimeMillis() ){
            log.error("=== 还没有到达开始时间 ===");
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }

        if (seckillProducts.getCount() <= seckillProducts.getSaled()){
            log.error("=== 总库存数量不足 ===");
            return BaseResponse.error(ErrorMessage.COUNT_NOT_ENOUGH);
        }


        return new BaseResponse(0, "判断通过！！！", null);

    }


}
