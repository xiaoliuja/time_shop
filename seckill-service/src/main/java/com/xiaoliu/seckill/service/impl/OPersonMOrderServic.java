package com.xiaoliu.seckill.service.impl;

import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.Constant;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.dao.SeckillOrderDao;
import com.xiaoliu.seckill.dao.SeckillProductsDao;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillOrder;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.service.OPersonMOrderService;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class OPersonMOrderServic implements OPersonMOrderService {


    @Autowired
    private SeckillProductsDao seckillProductsDao;

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 基本一人多单问题解决，在三层逻辑的校验之后，加根据用户id和订单id查询订单的校验；
     * 为0说明可以购买，为1说明用户已经购买过了该订单，返回error响应
     * @param req
     * @return
     */
    @Override
    public BaseResponse omOrder(SeckillSecondReq req) {


        log.info("===[开始三层逻辑校验]===");
        BaseResponse baseResponse = validateParam(req.getProductId(), req.getUserId());
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
            log.info("===[该用户重复下单！！！]===");
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
        log.info("==[生成订单成功]===");

        //扣减库存   多并发下扣减库存的详细操作前面已经讲过了，这里就不在详细的写了
        // 就写为普通的扣减库存了
        seckillProducts.setSaled(seckillProducts.getSaled()+1);
        seckillProducts.setUpdatedTime(new Date());
        seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);
        log.info("====【扣减库存成功】====");

        return BaseResponse.OK(true);
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


    /**
     * 悲观锁解决 多并发环境下的 一单多卖问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse smOrder(SeckillSecondReq req){
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





    /**
     *  悲观锁的第二种方式， 在判断是否有重复订单的时候添加 forUpdate；
     *  记得给productId和userId添加联合索引  不然不会加行锁
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse stmOrder(SeckillSecondReq req){
        log.info("===[开始三层逻辑校验]===");
        BaseResponse baseResponse = validateParam(req.getProductId(), req.getUserId());
        if (baseResponse.getCode() != 0){
            return baseResponse;
        }
        log.info("===[逻辑校验通过]===");


        log.info("===[开始一人多单校验]===");
        /**
         * 避免重复订单查询 ，   select count(*)  from seckill_order  where  user_id = **  and  product_id = **  for update。
         * 注：要给 user_id和 product_id 添加联合普通索引。
         */
        int count = seckillOrderDao.countForUpdate(req.getProductId(), req.getUserId());
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


    /**
     * 乐观锁   redis+联合唯一索引 保证一人多单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse redisOnlyOrder(SeckillSecondReq req) throws Exception {
        log.info("===[开始三层逻辑校验]===");
        BaseResponse baseResponse = validateParamByRedis(req.getProductId(), req.getUserId());
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
        int i = seckillProductsDao.updateByPrimaryKeySelective(seckillProducts);
        if (i == 0){
            log.error("===【秒杀失败，抛出异常，执行回滚】===");
            throw new Exception("库存不足");
        }

        log.info("执行kouj库存成功");




        //将成功购买该商品的用户的信息 produtcId： userId 用set数据结构放入到 redis当中
        try {
            String key1 = String.format(Constant.rediskey.SECKILL_PRODUCTID_USERID, req.getProductId());
            redisUtil.sSet(key1, req.getUserId());
        } catch (Exception e) {
            log.info("[记录已购用户时发生异常{}]",e);
        }


        return BaseResponse.ok(true);
    }

    //乐观锁判断
    private BaseResponse validateParamByRedis(Long productId,Long userId){
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
        //判断 redis中是否有 该key——value    该sHashKey API是跟绝 vlalue查询 keyv-value，因为value是唯一的吗，所以也只会查询一对值
        String key = String.format(Constant.rediskey.SECKILL_PRODUCTID_USERID, productId);
        if (redisUtil.sHasKey(key, userId)){
            log.info("不可重复购买该商品！！！");
            return BaseResponse.error(ErrorMessage.MANY_ORDER);
        }


        return new BaseResponse(0, "判断通过！！！", null);
    }


}
