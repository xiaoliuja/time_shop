package com.xiaoliu.seckill.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.Constant;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.dao.SeckillOrderDao;
import com.xiaoliu.seckill.dao.SeckillProductsDao;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillOrder;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.service.SeckillSecondService;
import com.xiaoliu.seckill.util.RedisCacheIfCount;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;


//秒杀
@Service
@Slf4j
public class SeckillSecondServiceImpl implements SeckillSecondService {


    @Autowired
    private SeckillProductsDao seckillProductsDao;

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private RedisCacheIfCount redisCacheIfCount;

//    @Autowired
//    private Redisson redisson;


    //基本秒杀逻辑
    @Override
/*    @Transactional(propagation = Propagation.REQUIRES_NEW)*/
    public BaseResponse sOrder(SeckillSecondReq req){
        log.info("===开始下单逻辑前的校验====");
        log.info("=== 三层逻辑校验");
        //编写 判断产品是否存在的方法
        BaseResponse response = validateParam(req.getProductId(), req.getUserId());
        if (response.getCode() != 0 ){
            return response;
        }
        log.info("校验已经合法");

        //获取到当前 商品信息的用户对象  因为前面的合法验证是封装在了一个方法中没有返回该对象
        Long id =  req.getProductId();
        SeckillProducts products = seckillProductsDao.selectByPrimaryKey(id);


        log.info("开始进行下单操作,创建订单");
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setProductId(req.getProductId());
        //这里的UserId我们从 Controller层中对其进行复制
        seckillOrder.setUserId(req.getUserId());
        seckillOrder.setProductName(products.getName());
        seckillOrderDao.insert(seckillOrder);
        log.info(" ====创建订单成功===");


        log.info("=== 开始扣减库存 ===");
        products.setSaled(products.getSaled()+1);
        seckillProductsDao.updateByPrimaryKeySelective(products);
        log.info("扣减库存成功！！！");


        return BaseResponse.OK(true);
    }


    //悲观锁 秒杀
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse pOrder(SeckillSecondReq req) {
        log.info("===开始悲观锁--下单逻辑前的校验====");
        log.info("=== 三层逻辑校验");
        //编写 判断产品是否存在的方法
        BaseResponse response = validateParamUpdate(req.getProductId(), req.getUserId());
        if (response.getCode() != 0 ){
            return response;
        }
        log.info("校验已经合法");

        //获取到当前 商品信息的用户对象  因为前面的合法验证是封装在了一个方法中没有返回该对象
        Long id =  req.getProductId();
        //因为前面的判断方法内部已经给这个 该条信息加上了行锁，他们这两条查询都是在同一个事务中，可以直接使用该数据，这里就不用进行加锁
        //难不成前面已经加上锁了，这里再去加锁？就不对了
        SeckillProducts products = seckillProductsDao.selectByPrimaryKey(id);


        log.info("开始进行下单操作,创建订单");
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setProductId(req.getProductId());
        //这里的UserId我们从 Controller层中对其进行复制
        seckillOrder.setUserId(req.getUserId());
        seckillOrder.setProductName(products.getName());
        seckillOrderDao.insert(seckillOrder);
        log.info(" ====创建订单成功===");


        log.info("=== 开始扣减库存 ===");
        products.setSaled(products.getSaled()+1);
        seckillProductsDao.updateByPrimaryKeySelective(products);
        log.info("扣减库存成功！！！");


        return BaseResponse.OK(true);
    }



    //乐观锁
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse oOrder(SeckillSecondReq req) throws Exception {
        log.info("===开始乐观锁--下单逻辑前的校验====");
        log.info("=== 三层逻辑校验");
        //编写 判断产品是否存在的方法
        BaseResponse response = validateParam(req.getProductId(), req.getUserId());
        if (response.getCode() != 0 ){
            return response;
        }
        log.info("校验已经合法");


        // 下单（乐观锁）
        return createOptimisticOrder(req.getProductId(), req.getUserId());

    }


    /**
     * Redis+lua脚本
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse luaOrder(SeckillSecondReq req) throws Exception{
        log.info("======[开始调用下单接口 Redis+lua]=======");

        log.info("=======[开始进行逻辑校验]===========");
        BaseResponse baseResponse = validateParam(req.getProductId(), req.getUserId());

        //需要定义再外部，因为发生异常会用到
        long res = 0;
        try {
            if(baseResponse.getCode()!=0){
                return baseResponse;
            }
            log.info("===[校验通过]====");


            //缓存库存扣减校验
            res =  redisCacheIfCount.decrStock(req.getProductId());
            if (res == 2){
                //缓存扣减库存成功

                //开始扣减库存  只进行了 saled = saled+1
                seckillProductsDao.updtaeSaled(req.getProductId());


                //开始创建订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setUserId(req.getUserId());
                seckillOrder.setProductId(req.getProductId());
                SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
                seckillOrder.setProductName(seckillProducts.getName());
                seckillOrderDao.insert(seckillOrder);

            }else {
                //说明 lua中 return的是0
                log.info("库存不足！！！");
                return BaseResponse.error(ErrorMessage.COUNT_NOT_ENOUGH);

            }
        } catch (Exception e) {
            /**我们这里为什么要 再这个  缓存扣减库存  数据库扣减库存  生成订单中加 try。。catch
            因为 一旦有个地方发生错误我们需要将 这个已经操作的数据进行回滚
             但是 缓存lua脚本只支持原子性 但是不会回滚   所以需要手动回滚
            **/
             log.error("==[异常！！]===",e);
            if (res==2){
                // 如果后面数据库操作失败，但这里成功的话 为了保证这个操作的原子性，需要把这里扣减的库存数量还原
                // 所以才再这个 Redis+lua操作的工具类中 加入了增加库存的方法
                redisCacheIfCount.addStock(req.getProductId());
            }

            throw new Exception("异常！！！");

        }
        return BaseResponse.ok(true);
    }


    //redission
    @Override
    public BaseResponse redissonOrder(SeckillSecondReq req) {
        log.info("===[开始调用下单接口]====");
        //key的名字
        String key = String.format(Constant.rediskey.SECKILL_DISTRIBUTE_LOCK, req.getProductId());
        //获取 redisson客户端锁
        try {
//            RLock rLock = redisson.getLock(key);
//            rLock.lock();


            BaseResponse baseResponse = validateParam(req.getProductId(), req.getUserId());
            if (Objects.isNull(baseResponse)){
                return baseResponse;
            }
            log.info("====[校验通过]====");

            log.info("===[开始扣减库存]===");
            SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(req.getProductId());
            seckillProducts.setSaled(seckillProducts.getSaled()+1);
            seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);

            log.info("生成订单");
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setProductId(req.getProductId());
            seckillOrder.setUserId(req.getUserId());
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setProductName(seckillProducts.getName());
            seckillOrderDao.insert(seckillOrder);

            return BaseResponse.ok(true);
        } catch (Exception e) {
            log.info("发生异常！！！");
            return BaseResponse.error(ErrorMessage.SYS_ERROR);
        }finally {
            /*rLock.unlock();*/
        }
    }




    //下单前 三层验证的判断   产品信息、开始时间、库存数量
    private BaseResponse validateParam(Long productId,Long UserId){
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(productId);

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



    //悲观锁  三层逻辑判断
    private BaseResponse validateParamUpdate(Long productId,Long userId){
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKeyForUpdate(productId);

        if (seckillProducts == null){
            log.error("---产品不存在---");
            return BaseResponse.error(ErrorMessage.PRODUCT_ERROR);
        }


        if (seckillProducts.getStartBuyTime().getTime() > System.currentTimeMillis()){
            log.error("===还没有到达开始时间===");
            return BaseResponse.error(ErrorMessage.SECKILL_NOT_START);
        }


        if (seckillProducts.getCount() <= seckillProducts.getSaled()){
            log.error("=== 总库存数量不足 ===");
            return BaseResponse.error(ErrorMessage.COUNT_NOT_ENOUGH);
        }


        return new BaseResponse(0, "判断通过！！！", null);

    }








    //乐观锁创建订单逻辑
    private BaseResponse createOptimisticOrder(Long productId,Long userId) throws Exception {
        log.info("===[乐观锁下单逻辑Staratiing]====");
        //创建订单
        SeckillProducts seckillProducts = seckillProductsDao.selectByPrimaryKey(productId);

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setProductId(productId);
        seckillOrder.setCreateTime(new Date());
        //控制层会将该 userId传进去，看了一晚上操，这传成 产品id了，真是个傻逼
        seckillOrder.setUserId(userId);

        seckillOrderDao.insert(seckillOrder);
        log.info("===【创建订单成功】===");


        //扣减库存
        int i = seckillProductsDao.updateByPrimaryKeyOptimistic(productId);
        if (i==0){
            log.error("==秒杀失败，抛出异常，执行回滚");
            throw new Exception("库存不足");
        }
        log.info("===[库存扣减成功!]===");
        return BaseResponse.ok(true);

    }


}
