package com.xiaoliu.seckill.config;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//redission客户端的初始化
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.database}")
    private String database;


    /*public Redisson getRedisson() {
        Config config = new Config();
        //单机模式  依次设置redis地址和密码
        config.useSingleServer()
                .setPassword(password)
                .setAddress("redis://" + host + ":" + port).setDatabase(Integer.valueOf(database));*/


        //主从
//        config.useMasterSlaveServers()
//                .setMasterAddress("127.0.0.1:6379")
//                .addSlaveAddress("127.0.0.1:6389", "127.0.0.1:6332", "127.0.0.1:6419")
//                .addSlaveAddress("127.0.0.1:6399");
        //哨兵
//        config.useSentinelServers()
//                .setMasterName("mymaster")
//                .addSentinelAddress("127.0.0.1:26389", "127.0.0.1:26379")
//                .addSentinelAddress("127.0.0.1:26319");
        //集群
//        config.useClusterServers()
//                .setScanInterval(2000) // cluster state scan interval in milliseconds
//                .addNodeAddress("127.0.0.1:7000", "127.0.0.1:7001")
//                .addNodeAddress("127.0.0.1:7002");


    /*    return (Redisson) Redisson.create(config);
    }*/
}
