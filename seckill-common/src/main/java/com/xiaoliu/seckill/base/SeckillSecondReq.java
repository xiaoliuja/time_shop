package com.xiaoliu.seckill.base;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SeckillSecondReq implements Serializable {

    @NotNull(message = "产品id 不能为空！！！")
    private Long productId;

    private Long userId;

    @Data
    public static class seckillGetVerify extends SeckillSecondReq implements Serializable{
        //NOTNULL 知识不能为空，而notblank不仅不能为空，也不能为空字符串
        @NotBlank(message = "验证码不能为空 ！！！")
        private String decode;
    }
}
