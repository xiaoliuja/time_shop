package com.xiaoliu.seckill.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

//基本辅助类，把SeckillUser转换为CommonWebUser
@Data
public class CommonWebUser implements Serializable {
    private Long id;

    private String name;

    private String phone;

    private Date createTime;

    private Date updateTime;


}
