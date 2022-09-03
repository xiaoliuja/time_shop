package com.xiaoliu.seckill.model.Base;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserResp implements Serializable {

    @Data
    //登录成功后把用户的身份信息存放在 token中去，响应回去
    public static class LoginUserResp implements Serializable{
           private String token;
    }
}
