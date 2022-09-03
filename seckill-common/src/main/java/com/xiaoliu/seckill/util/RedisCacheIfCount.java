package com.xiaoliu.seckill.util;

import com.google.common.collect.Lists;
import com.xiaoliu.seckill.base.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RedisCacheIfCount {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    //初始化lua脚本用到了，直接定义了该类型的变量
    private DefaultRedisScript<Long> getRedisScript;


    //定义了lua脚本，直接编写了，其实也可以从固定路径加载进来
    private String subStock = "local key=KEYS[1];\n" +
            "local surplusStock = tonumber(redis.call('get',key));\n" +
            "if (surplusStock<=0) then return 0\n" +
            "else\n" +
            "    redis.call('incrby', key, -1)\n" +
            "    return 2 \n" +
            "end";


    //AOP接口，在服务器启动的时候就去加载一些东西，这加载的就是 lua脚本和这个结果类型
    @PostConstruct
    public void init(){
        getRedisScript = new DefaultRedisScript<Long>();
        //设置一下减库存的这个返回值
        getRedisScript.setResultType(Long.class);
        //加载lua脚本
        getRedisScript.setScriptText(subStock);
    }


    /**
     * 减库存
     */
    public Long decrStock(Long productId){
        //redis执行lua脚本，   将 这个SECKILL_SALED_COUNT变量 的%s换为 productId

        Long res = redisTemplate.execute(getRedisScript, Lists.newArrayList(String.format(Constant.rediskey.SECKILL_SALED_COUNT, productId)));
        return res;
    }


    /**
     * 加库存
     */
    public void addStock(Long productId){
        String key = String.format(Constant.rediskey.SECKILL_SALED_COUNT, productId);
        if (redisTemplate.hasKey(key)){
            redisTemplate.opsForValue().increment(key);
        }

    }


}
