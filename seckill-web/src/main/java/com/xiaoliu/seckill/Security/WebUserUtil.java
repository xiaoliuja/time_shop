package com.xiaoliu.seckill.Security;

import com.alibaba.fastjson.JSONObject;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.util.RedisUtil;
import com.xiaoliu.seckill.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 获取当前用户对象的一个工具类
 * 我们的拦截器中只是用了一个这个内部定义的这个静态变量
 */
public class WebUserUtil {

    //我们拦截器中 session中存储用户对象信息的 变量web_user_key
    public static final String SESSION_WEBUSER_KEY = "web_user_key";

    /**
     *  获取当前用户
     */
    public static CommonWebUser getLoginUser(){

        //获取到当前的相关对象 接收到请求，记录请求内容
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //将其转换为 request对象  获取请求request
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取sesion
        HttpSession session = request.getSession();

        /**
         *  这其实就跟 过滤器内部用户登录的拦截的逻辑差不多；
         *  判断session，如果session内部有值的话，把该对象信息封装为一个commonwebuser对象
         *  如果内部没值的话，说明没登录根据客户端请求传过来的 token 去redis 里面获取到用户对象
         *  我们再以前编写登录的时候，登陆成功后内部存储的为对象的json且将用户对象信息赋值给了coomonwebuser对象
         *  redis内部key为token，value为json格式的对象信息  所以当我们从redis里面取出对象时也是为 json格式的commonwebuser对象的信息
         */

        CommonWebUser commonWebUser = null;
        if(session.getAttribute(SESSION_WEBUSER_KEY) != null){
            //用户登陆了想获取用户对象
            //这里也就说明用户登陆了     内部的session有对象信息    我们其实过滤器的时候在使用session时，session里面放的也都为 封装的 commonwebuser对象
            commonWebUser = (CommonWebUser) session.getAttribute(SESSION_WEBUSER_KEY);
        }else {
            //用户没登录想获取当前用户对象

            //通过  我们创建的一个普通工具类SpringContextHolder去获取spring内部的bean
            //获取 RedisUtils对象
            RedisUtil redisUtil = SpringContextHolder.getBean("redisUtil");

            //因为美欧登录 那就判断客户端的请求是否带有token，根据token去获取对象
            if(StringUtils.isNotEmpty(request.getHeader("token"))){
                //如果不为空的话，那么就获取token去redis内部根据token获取用户对象信息
                Object object = redisUtil.get(request.getHeader("token"));
                if ( object != null){
                    /**如果 redis.get不为空，那说明有该token对应的信息
                     * 将存储的json对象 取出解析为  CommonWebUser对象，
                     * 因为我们登陆成功的时候，将user对象复制给了CommonWebUser对象存了进去
                     */
                   commonWebUser = JSONObject.parseObject(object.toString(), CommonWebUser.class);
                   //再将获取到的对象用户存入我们的sesion里面去
                   //所以说如果我们使用了该方法，这个session内部也会改变为我们存储的对象
                   session.setAttribute(SESSION_WEBUSER_KEY, commonWebUser);
                }
            }

        }

        return commonWebUser;

    }

}
