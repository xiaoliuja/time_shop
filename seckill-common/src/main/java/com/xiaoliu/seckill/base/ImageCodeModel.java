package com.xiaoliu.seckill.base;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class ImageCodeModel implements Serializable {

    @NotEmpty(message = "imageId不能为空")
    private String imageId;
    @NotEmpty(message = "imageStr不能为空")
    private String imageStr;
}
