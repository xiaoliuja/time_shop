package com.xiaoliu.seckill.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.xiaoliu.seckill.Security.WebUserUtil;
import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.base.SeckillSecondReq;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.LimitUserService;
import com.xiaoliu.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;


/**
 * 限流解决  秒杀用户量峰值剧增的情况下 系统保持稳定不崩溃
 */
@RestController
@Slf4j
@RequestMapping("/limitUser")
public class CurrentLimitController {

    @Autowired
    private UserService userService;

    @Autowired
    private LimitUserService limitUserService;

    /**
     * 服务端 令牌桶算法 限流
     */
    @RequestMapping("/bucket")
    public BaseResponse limitBucket(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        log.info("===[成功获取到了用户对象]===");

        SeckillSecondReq data = req.getData();
        SeckillUser seckillUser = userService.findAllByPhone(loginUser.getPhone());
        data.setUserId(seckillUser.getId());

        return  limitUserService.limitBucketUser(data);
    }


    /**
     * 服务端 redis+lua脚本
     */
    @RequestMapping("/redisLua")
    public BaseResponse limitRedisLua(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        log.info("===[成功获取到了用户对象]===");

        SeckillSecondReq data = req.getData();
        SeckillUser seckillUser = userService.findAllByPhone(loginUser.getPhone());
        data.setUserId(seckillUser.getId());

        return  limitUserService.orderV1(data);
    }



    /**
     * 服务端 nginx+lua
     */
    @RequestMapping("/nginxLua")
    public BaseResponse limitNginxLua(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        log.info("===[成功获取到了用户对象]===");

        SeckillSecondReq data = req.getData();

        SeckillUser seckillUser = userService.findAllByPhone(loginUser.getPhone());
        data.setUserId(seckillUser.getId());

        return  limitUserService.createOrder(data);
    }


    /**
     * 开始前的秒杀地址在秒杀开始后，调用发送验证码
     */
    @RequestMapping("/verifyDecode")
    public BaseResponse<String> getVerify(@Valid @RequestBody BaseRequest<SeckillSecondReq> req){
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillUser user1 = userService.findAllByPhone(user.getPhone());
        SeckillSecondReq data = req.getData();
        data.setUserId(user1.getId());

        return limitUserService.getVerifyDecode(data);
    }


    /**
     * 隐藏真实秒杀接口的url地址，拿到验证码判断是否合法， 再做相应的秒杀操作
     */
    @RequestMapping("/hideOrder")
    private BaseResponse orderHide(@Valid @RequestBody BaseRequest<SeckillSecondReq.seckillGetVerify> req){
        CommonWebUser loginUser = WebUserUtil.getLoginUser();
        if (Objects.isNull(loginUser)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }

        SeckillUser phoneUser = userService.findAllByPhone(loginUser.getPhone());
        SeckillSecondReq.seckillGetVerify data = req.getData();
        data.setUserId(phoneUser.getId());

        return limitUserService.orderHide(data);
    }


    /**
     *
     */




}
