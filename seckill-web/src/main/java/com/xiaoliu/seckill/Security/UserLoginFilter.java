package com.xiaoliu.seckill.Security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoliu.seckill.base.BaseResponse;
import com.xiaoliu.seckill.base.CommonWebUser;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

@Slf4j
@WebFilter(filterName = "UserLoginFilter",urlPatterns = "/*")
public class UserLoginFilter implements Filter {

    @Autowired
    private RedisUtil redisUtil;

    //默认拦截所有，关联配置文件中的拦截路径
    @Value("${auth.login.pattern}")
    private String urlPattern;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //把超类转换为 HttpServletRequest和 HttpServletResponse
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();

        //  拿到url的地址   http://localhost:8080  后面的问号前面的
        String url = request.getRequestURI();
        log.info("url 当前请求的路径:=" + url + ",pattern 设定的拦截的路径" + urlPattern);

        //进行拦截路径的匹配
        if (url.matches(urlPattern)){
            if (session.getAttribute(WebUserUtil.SESSION_WEBUSER_KEY) != null){
                //说明 sesion内部存储着用户信息，用户已经登陆了，不需要进行这个处理了
                filterChain.doFilter(request, response);
                return;
            }else {
                //token为我们 客户端传过来的，保存在header中，也可以保存在cookie中
                //  调用我们接口的前端或客户端也会保存在cookie，具体使用方式由公司决定
                String tokenValue = request.getHeader("token");
                if(StringUtils.isNotEmpty(tokenValue)){
                    // 判断token不为空的话   根绝该token去redis内部获取CommonWebUser对象信息
                    Object o = redisUtil.get(tokenValue);
                    if (o != null){
                        // redis内部有对象信息的话  解析json字符串为 commonWebUser对象
                        CommonWebUser commonWebUser = JSONObject.parseObject(o.toString(), CommonWebUser.class);
                        session.setAttribute(WebUserUtil.SESSION_WEBUSER_KEY,commonWebUser);
                        //保存在 session中
                        filterChain.doFilter(request, response);
                        return;
                    }else {
                        //如果根据token去redis里获取为空的话
                        //我们定义的 登录的错误码方法
                        returnJSON(response);
                        return;
                    }

                }else {
                    //如果从请求头header中获取的token为空的话，也返回登录的错误码
                    returnJSON(response);
                    return;
                }

            }

        }

        // filterChain  使用该方法可以调用过滤器链中的下一个Filter的doFilter方法；若该filter是最后一个，则回去调用目标资源
        /**
         * 过滤器执行完chain.dofilter(req,resp)后,放行到你所在的servlet或jsp,执行完servlet或者jsp后，
         * 或重新回到过滤器执行完剩余代码，要是你在剩余代码中又有请求发出，程序就会发生发出多次请求错误。
         * 总的来说，就是chain.dofilter(req,resp)下面的代码不能有请求，如果有，请加上return。
         */
        filterChain.doFilter(request, response);
        return;

    }


    //报异常的处理方法
    public void returnJSON(ServletResponse response){
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {

            writer = response.getWriter();
            BaseResponse baseResponse = new BaseResponse(ErrorMessage.USER_NEED_LOGIN.getCode(), ErrorMessage.USER_NEED_LOGIN.getMessage(), null);

            // 将 Servlet 中的数据直接输出到客户端上的
            writer.print(JSON.toJSONString(baseResponse));


        } catch (IOException e) {
            log.info("response error",e);
        }finally {
            if (writer != null){
                writer.close();
            }
        }

    }



}
