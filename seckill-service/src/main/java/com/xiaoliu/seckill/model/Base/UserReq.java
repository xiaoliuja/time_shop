package com.xiaoliu.seckill.model.Base;

import com.fasterxml.jackson.core.SerializableString;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UserReq implements Serializable {

    //发送验证码验证  数据库中是否有该手机号
    @Data
    public static class PhoneCodeReq implements Serializable{
        @NotNull(message = "手机号不能空")
        private String phone;
    }


    //  登录时短信验证码     继承了PhoneCodeReq所以不用首序列化
    @Data
    public static class LoginUserReq extends PhoneCodeReq {
        @NotNull(message = "短信验证码不能为空 ！！！")
        private String dxCode;

    }

}
