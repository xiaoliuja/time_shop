package com.xiaoliu.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class DistrubuteLimit {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    private DefaultRedisScript<Long> defaultRedisScript;

    // redis进行key的增加操作，当key的增加次数value，为6的时候，return 0；
    public static final String LIMIT_LUA =
                                            "local times = redis.call('incr',KEY[1])\n" +
                                            "if times == 1 then\n" +
                                            "   redis.call('expire',KEYS[1],ARGV[1]\n)" +
                                            "end\n" +
                                            "if times > tonumber(ARGV[2]) then\n" +
                                            "   return 0\n" +
                                            "end\n" +
                                            "return 1";


    @PostConstruct
    public void init(){
        defaultRedisScript = new DefaultRedisScript<Long>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText(LIMIT_LUA);
    }


    public boolean exec() {
        List<String> keyList = new ArrayList<>();
        String key = "ip:" + System.currentTimeMillis()/1000 ;   //每秒生成一个key
        keyList.add(key);
        /**
         * 这样调用redis调用lua脚本时，在一秒内对同一个key进行增加操作
         * 如果在这样一秒内，key增加的次数超过6， 我们就返回错误；
         * 这样就可以在这 1s内限制用户的请求最多只能有 6个
         * 从而 起到限流 的作用
         */
        return redisTemplate.execute(defaultRedisScript, keyList, 3000,6) == 1 ? true : false;

    }



}
