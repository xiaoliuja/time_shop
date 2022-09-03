package com.xiaoliu.seckill.base;

public interface Constant {

    String FAIL = "FAIL";
    String SUCCESS  = "SUCCESS";

    interface rediskey{
        /**
         * 分布式锁的KEY
         * sk:d:lock:商品id
         */
        String SECKILL_DISTRIBUTE_LOCK = "sk:d:lock:%s";






        /**
         * 商品id的缓存库存数量
         * key sk:sc:商品id
         * %s就是去匹配这个商品id的    sk:sc:商品id
         */
        String SECKILL_SALED_COUNT = "sk:sc:%s";


        /**
         * 一人多单 redis缓存  key
         * key  om:redis:商品id
         */
        String SECKILL_PRODUCTID_USERID = "om:redis:%s";


        /**
         * redis的 key
         */
        String VERIFY_DECODE_KEY = "sk:mit:%s:u:%s";


        /**
         *  redis的value前缀
         */
        String VERIFY_DECOED_SALT = "sk:limit:salt";


        /**
         * redis内部存储的 图片验证码对应的key
         */
        String SECKILL_IMAGE_CODE = "sk:im:code:%s";


        /**
         * 限制用户访问的频次
         */
        String SECKILL_USER_COUNT = "sk:user:%s:count:%s";
    }

}
