package com.xiaoliu.seckill.exception;

public enum ErrorMessage {

    //枚举
    SYS_ERROR(10000,"系统开小差了，清稍后再试"),
    PARM_ERROR(10001,"参数错误"),
    CODE_ERRE(10006,"验证码信息错误"),
    USER_NEED_LOGIN(10007,"用户信息出错了"),
    PRODUCT_ERROR(100010,"产品信息不存在"),
    SECKILL_NOT_START(100011,"秒杀还未开始"),
    COUNT_NOT_ENOUGH(100012,"库存数量不足"),
    MANY_ORDER(100013,"不可重复购买该商品"),
    LIMIT_USER_MESSAGE(100014,"用户被限流"),
    ERROR_DECODE(100015,"验证码错误"),
    IMAGE_code_ERROR(100016,"图片校验未通过"),
    USER_COUNT_TOO_MANY(100017,"访问次数过多");




    private Integer code;
    private String message;

    ErrorMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public ErrorMessage setMessage(String message) {
        this.message = message;
        return this;
    }

}
