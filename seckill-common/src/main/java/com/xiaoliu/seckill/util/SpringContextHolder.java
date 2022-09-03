package com.xiaoliu.seckill.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;



    //实现ApplicationContext接口的context注入函数，并将其存入静态变量
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }



    /**
     * 取得静态变量中的ApplicationContext
     */
    public static ApplicationContext getApplicationContext(){
        checkApplicationContext();
        return applicationContext;
    }



    //从静态变量ApplicationContext中取得Bean，自动转型为所赋值对象的类型
    //	unchecked 抑制没有进行类型检查操作的警告
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name){
         checkApplicationContext();
         //根据 id获取 IOC容器中的Bean
         return (T) applicationContext.getBean(name);
    }



    //从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz){
        checkApplicationContext();
        //根据 类的类型获取bean，如果bean不是唯一的，那就无法获取
        //如果配置文件中配置了两个或者更多的bean,用类型返回的方法是不可以具体到哪个bean的）.
        return (T) applicationContext.getBeansOfType(clazz);
    }



    /**
     * 清除applicationContext静态变量
     */
    public static void cleanApplicationContext(){
        applicationContext = null;
    }





    //验证 applicationContext是否为 null
    private static void checkApplicationContext(){
        if (applicationContext == null){
            throw new IllegalStateException("applicationContext未注入，请在applicationContext.xml中定义springContextHolder");
        }
    }

}
