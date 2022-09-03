package com.xiaoliu.seckill.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.xiaoliu.seckill.Security.WebUserUtil;
import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.dao.SeckillOrderDao;
import com.xiaoliu.seckill.dao.SeckillUserDao;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.OPersonMOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.print.BackgroundServiceLookup;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/ManyOrder")
public class OPersonMOrderControlelr {

    @Autowired
    private OPersonMOrderService mOrderService;


    @Autowired
    private SeckillUserDao seckillUserDao;


    //基本的一人多单问题解决
    @PostMapping("/basicMorder")
    public BaseResponse bmOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillSecondReq data = req.getData();
        SeckillUser phoneUser = seckillUserDao.findAllByPhone(loginUser.getPhone());
        data.setUserId(phoneUser.getId());

        BaseResponse baseResponse = mOrderService.omOrder(data);
        return baseResponse;
    }


    //多并发环境下悲观锁的一人多单解决
    @RequestMapping("/msOrder")
    public BaseResponse msOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillSecondReq data = req.getData();
        SeckillUser phoneUser = seckillUserDao.findAllByPhone(loginUser.getPhone());
        data.setUserId(phoneUser.getId());
        return mOrderService.smOrder(data);
    }




    /**
     * 悲观锁的第二种方式， 在判断是否有重复订单的时候添加 forUpdate；
     *      *  记得给productId和userId添加联合索引  不然不会加行锁
     * @param req
     * @return
     */
    @RequestMapping("/stmOrder")
    public BaseResponse stmOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillSecondReq data = req.getData();
        SeckillUser phoneUser = seckillUserDao.findAllByPhone(loginUser.getPhone());
        data.setUserId(phoneUser.getId());
        return mOrderService.stmOrder(data);
    }


    /**
     * redis+唯一索引
     */
    @RequestMapping("/optimisticOrder")
    public BaseResponse opOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        try {
            CommonWebUser loginUser = WebUserUtil.getLoginUser();
            if (Objects.isNull(loginUser)){
                return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
            }

            SeckillSecondReq data = req.getData();
            SeckillUser phoneUser = seckillUserDao.findAllByPhone(loginUser.getPhone());
            data.setUserId(phoneUser.getId());

            return mOrderService.redisOnlyOrder(data);
        } catch (Exception e) {
            log.info("秒杀失败");
        }
        //过来这就出错了，说明上面没有return出去
        //注： catch中return或者finall中return 和 方法最后return只能有一个
        return BaseResponse.error(ErrorMessage.SYS_ERROR);
    }


}
