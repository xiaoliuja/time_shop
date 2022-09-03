package com.xiaoliu.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.xiaoliu.seckill.Security.WebUserUtil;
import com.xiaoliu.seckill.base.BaseRequest;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.Base.UserReq;
import com.xiaoliu.seckill.model.Base.UserResp;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.UserService;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/login")
public class LoginController {

    //作为 redis 内部key值的一部分
    private final String USER_PHONE_CODE = "upc";

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;


    //根据手机号判断 用户是否已经注册过（数据库中是否有对应的手机号的信息）
    @PostMapping("/getPhoneSendYZcode")
    public BaseResponse<Boolean> getPhoneSmsCode(@Valid @RequestBody BaseRequest<UserReq.PhoneCodeReq> req){
        //从这个 基础的请求格式内部拿到数据getdata，然后在拿到数据的具体参数
        String phone = req.getData().getPhone();

        SeckillUser user = userService.findByphone(phone);

        //判断用户存在与否
        if(user != null){
            /**
             *  如果存在, 将发送短信验证码，其实应该调用第三方接口（一般都有sdk，解耦库都http的，可以使用httpclient发送数据）给用户发送验证码
             *  我们这里就不用http接口去调用了，知道这个真实场景中是这么干的就行。
             *  我们直接String一个验证码, 将其存到这个reids里面去。
             */
            //验证码
            String random = "987654";
            /**
             *  放入redis里面去  为了这个手机号用户key的唯一性（每个手机号用户在redis里面生成的key都不一样），
             *  当相同的手机号如果在发送验证码的时候，由于key是相同的，存放验证码的value内部的值就会被新的验证码覆盖
             *  我们在外面定义一个常量，常量+该用户的手机号作为key去存储，然后value存储的就是验证码；
             *  然后给这个验证码设置一个过期时间，过期后用户再输该验证码登录就不管用了  设置3min
             */
            redisUtil.set(USER_PHONE_CODE+phone,random,60*60);

            return BaseResponse.ok(true);
        }else{

            return BaseResponse.ok(false);
        }
    }



    //用户登录
    @PostMapping("/userPhoneLogin")
    public BaseResponse<UserResp.LoginUserResp> userPhoneCodeLogin(@Valid @RequestBody BaseRequest<UserReq.LoginUserReq> userReq){
       //通过请求的数据 找到对应的key去redis里面拿验证码
        UserReq.LoginUserReq data = userReq.getData();
        /**
         *  该redis的dget方法会判断key是否等于null，为null的话就返回 null；
         *  否哦则的话就将 key存储到 object对象中给返回
         */
        Object o = redisUtil.get(USER_PHONE_CODE + data.getPhone());

        //判断查询的对象是否为空或者对象内的值跟用户输入的验证码对应不上
        if (o == null || !o.toString().equals(data.getDxCode())){
            //如果 false的话， 返回错误
            return BaseResponse.error(ErrorMessage.CODE_ERRE);
        }else
        {
            //说明用户验证码信息通过了

            //删除 redis内的存储用户的验证码信息
            redisUtil.del(USER_PHONE_CODE + data.getPhone());

            //接下来就是把用户的信息（姓名和电话）给存放在 token中去。

            //1.首先根绝这个用户的电话phone把这个 用户的信息给查出来
            SeckillUser successUser = userService.findByphone(data.getPhone());

            //2.这里去定义一个相同的对象，将当亲的SeckillUsr对象给 copy进去

            // 项目亮点：编写了一个普通的类去获取到 当前代码中spring内部注入的bean 也就是我们的 SeckillUser
            //我们在编写一个 与User对象参数一样的 CommonWebUser类
            CommonWebUser commonWebUser = new CommonWebUser();
            //将查询到的对象的内容  复制给 CommonWebUser对象
            BeanUtils.copyProperties(successUser,commonWebUser);

            //利用 UUID 生成一个 唯一id   replaceAll将有”-“的地方替换为空
            String token = UUID.randomUUID().toString().replaceAll("-", "");

            /**
             * 将这个 token 存放入redis里面去; ket为 token， value为对象的信息
             * 需要将对象转换为String，因为value是String类型的  JSON.toJSONString将对象解析为 JSON字符串
             * 过期时间为30天
             */
            redisUtil.set(token, JSON.toJSONString(commonWebUser),60*60*24*30);

            //将token返回出去  也就是返回给 客户端，客户端以后每次访问的时候就会拿着该token
            UserResp.LoginUserResp userResp = new UserResp.LoginUserResp();
            userResp.setToken(token);


            return  BaseResponse.OK(userResp);
        }
    }


    //测试一下   用普通方法些写的获取当前用户  写的那个工具类
    @GetMapping("/checkUserToken")
    public String checkUserToken(){
        CommonWebUser commonWebUser = WebUserUtil.getLoginUser();
        return JSON.toJSONString(commonWebUser);
    }


}
