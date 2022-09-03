package com.xiaoliu.seckill.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
public class VerifyCodeUtil {

    private static Properties props = new Properties();


    @Bean
    public DefaultKaptcha defaultKaptcha(){
        //创建 DefaultKaptcha对象
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();

        //读取配置文件
        try {
            props.load(VerifyCodeUtil.class.getClassLoader().getResourceAsStream("kaptcha.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //把这个配置让赋给 该defaultKaptcha对象
        defaultKaptcha.setConfig(new Config(props));
        return defaultKaptcha;
    }

}
