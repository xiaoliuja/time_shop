package com.xiaoliu.seckill.security;

import com.fasterxml.jackson.core.filter.TokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * 创建 WebSecurityConfig类 继承 WebSecurityConfigurerAdapter
 * 类上加 @EnableWebSecurity，注解中包括 @Configuration
 * WebSecurityConfigurerAdapter声明了一些默认的安全特性
 * （1）验证所有的请求
 * （2）可以使用 springSecurity 默认的表单页面进行验证登录
 * （3）允许用户使用http请求进行验证
 */


/*@EnableWebSecurity*/
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /*//AuthenticationManagerBuilder用户认证实现类
    @Autowired
    private MyUserDetailService myUserDetailService;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;
    @Autowired
    private TokenFilter tokenFilter;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    //生成加密方式的密码
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);
    }

    // http权限认证
    @Override
    protected void configure(HttpSecurity http) throws Exception {


        *//**硬式编码定义拦截路径
         *  authorizeRequests目的是指定url进行拦截的，也就是默认这个url是“/”也就是所有的
         *          * anyanyRequest（）、antMatchers（）和regexMatchers（）三种方法来拼配系统的url。并指定安全策略
         *//*
        http.authorizeRequests()
                .antMatchers("/product/**").hasAuthority("ADMIN")
                        .antMatchers("/admin/**").hasAuthority("SUPERADMIN")
                        .antMatchers("/login","/user/**").permitAll();

        //固定写法   所有请求都必须认证才能访问，必须登录
        http.authorizeRequests()
                .anyRequest()
                        .authenticated();

        //exception处理相关的辅助类
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
        http.cors();    //防止前端多个域名的情况
        http.csrf().disable();



        super.configure(http);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }*/
}
