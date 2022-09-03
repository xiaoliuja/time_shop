package com.xiaoliu.seckill.controller;

import com.xiaoliu.seckill.Security.WebUserUtil;
import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.SeckillSecondService;
import com.xiaoliu.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/seckill")
public class SeckillSecondController {

    @Autowired
    private SeckillSecondService seckillSecondService;

    @Autowired
    private UserService userService;


    /**
     * 原始下单逻辑（其实就是普通的抢购逻辑）
     * @param req
     * @return
     */
    @PostMapping("/order")
    public BaseResponse  seckillOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){

        //先这个验证 token  直接利用获取当前对象的工具类 WebUserUtil的 getLoginUser方法去获取当前用户对象
        /**
         *  这里里面内部其实就是写了一个，因为我们的用户对象是保存安东 session里面的吗 如果说session里有值说明目前用户已处于登录状态，将对象直接返回
         *  如果说没值，再去判断用户用户是否携带了 token过来，携带的化根据 验证 token去拿到用户的对象信息
         */

        CommonWebUser commonWebUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(commonWebUser)){
            //没拿到用户
            BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        //否则的话说明用户已处于登录状态，就去 进行三层验证  产品信息是否存在、秒杀活动是否开始、总库存数量是否足够
        SeckillSecondReq data = req.getData();
        //因为我们的这个 service层内部的 创建订单中需要添加 userId，因为user表中没有与其他表关联的字段所以无法获取，所以通过 Controller层中获取到的当前用户对象给它赋值进去
        data.setUserId(commonWebUser.getId());


        //主要的秒杀的三层判断以及 后面的 下单扣减库存逻辑
        BaseResponse baseResponse = seckillSecondService.sOrder(data);
        return baseResponse;

    }


    /**
     * 避免超卖-  Jvm锁  Synchronized
     */
    @PostMapping("/order/synchronized")
    public synchronized BaseResponse  seckillOrderSynchronized(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){

        //先这个验证 token  直接利用获取当前对象的工具类 WebUserUtil的 getLoginUser方法去获取当前用户对象
        /**
         *  这里里面内部其实就是写了一个，因为我们的用户对象是保存安东 session里面的吗 如果说session里有值说明目前用户已处于登录状态，将对象直接返回
         *  如果说没值，再去判断用户用户是否携带了 token过来，携带的化根据 验证 token去拿到用户的对象信息
         */

        CommonWebUser commonWebUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(commonWebUser)){
            //没拿到用户
            BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        //否则的话说明用户已处于登录状态，就去 进行三层验证  产品信息是否存在、秒杀活动是否开始、总库存数量是否足够
        SeckillSecondReq data = req.getData();
        //因为我们的这个 service层内部的 创建订单中需要添加 userId，因为user表中没有与其他表关联的字段所以无法获取，所以通过 Controller层中获取到的当前用户对象给它赋值进去
        data.setUserId(commonWebUser.getId());


        //主要的秒杀的三层判断以及 后面的 下单扣减库存逻辑
        BaseResponse baseResponse = seckillSecondService.sOrder(data);
        return baseResponse;

    }


    /**
     * 悲观锁-解决超卖
     */
    @PostMapping("/order/sadLock")
    public BaseResponse pOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){

        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillSecondReq data = req.getData();
        data.setUserId(loginUser.getId());
        return seckillSecondService.pOrder(data);
    }




    // 秒杀下单-乐观锁
    @RequestMapping("/optimistic/roder")
    public BaseResponse sOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){

        //因为下层 service在进行更新操作判断库存数量时候，数量不足抛出异常所以在 Controller层需要捕获。
        try {
            CommonWebUser user = WebUserUtil.getLoginUser();
            if (Objects.isNull(user)){
                return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
            }
            SeckillSecondReq data = req.getData();

            SeckillUser phoneUser = userService.findAllByPhone(user.getPhone());

            data.setUserId(phoneUser.getId());
            return seckillSecondService.oOrder(data);

        } catch (Exception e) {
            log.error("===[秒杀异常！]===");

        }
        return BaseResponse.error(ErrorMessage.SYS_ERROR);
    }


    //Redis+lua
    @PostMapping("/redisLua/order")
    public BaseResponse luaOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){



        try {
            CommonWebUser loginUser = WebUserUtil.getLoginUser();
            if (Objects.isNull(loginUser)){
                return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
            }
            SeckillSecondReq data = req.getData();
            SeckillUser user = userService.findAllByPhone(loginUser.getPhone());
            data.setUserId(user.getId());

             return seckillSecondService.luaOrder(data);
        } catch (Exception e) {
            log.error("秒杀异常！！！");
        }

        return BaseResponse.error(ErrorMessage.SYS_ERROR);

    }


    /**
     * 秒杀下单（避免超卖问题）-redission
     */
    @RequestMapping("/redission/order")
    public BaseResponse rOrder(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillSecondReq data = req.getData();
        SeckillUser seckillUser = userService.findAllByPhone(user.getPhone());
        data.setUserId(seckillUser.getId());
        return seckillSecondService.redissonOrder(data);
    }


}
