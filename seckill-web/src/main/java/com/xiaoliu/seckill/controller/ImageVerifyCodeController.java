package com.xiaoliu.seckill.controller;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.xiaoliu.seckill.Security.WebUserUtil;
import com.xiaoliu.seckill.base.*;
import com.xiaoliu.seckill.exception.ErrorMessage;
import com.xiaoliu.seckill.model.SeckillUser;
import com.xiaoliu.seckill.service.UserService;
import com.xiaoliu.seckill.service.SeckillImageService;
import com.xiaoliu.seckill.util.RedisUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Objects;

/**
 * 图片验证码
 */
@Controller
@Slf4j
public class ImageVerifyCodeController {

    @Autowired
    private DefaultKaptcha defaultKaptcha;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private SeckillImageService seckillImageService;

    @SneakyThrows
    @RequestMapping("/seckill/code")
    @ResponseBody
    public BaseResponse<ImageCodeModel> getNumCode(HttpServletResponse response, HttpServletRequest request){
        //获得二进制输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //通过DefaultKaptcha获得随机验证码
        String textCode = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(textCode);
        //将图片写入流中
        ImageIO.write(image, "jpg", baos);

        //生成的一个随机Id0
        String imageId = IdUtil.objectId();
        ImageCodeModel vo = new ImageCodeModel();
        vo.setImageId(imageId);
        //为了保证安全加密
        vo.setImageStr(Base64.getEncoder().encodeToString(baos.toByteArray()));
        //将 随机生成的imageId和这个defaultKaptcha生成的验证码一一对应，存到redis中。在校验的时候会用到
        redisUtil.set(String.format(Constant.rediskey.SECKILL_IMAGE_CODE, imageId), textCode,60);

        //将这个返回给前端要用
        return BaseResponse.OK(vo);
    }


    /**
     * 验证输入的图片验证码是否正确的接口，后面为秒杀逻辑
     * @param req
     * @return
     */
    @RequestMapping("/v3/order")
    @ResponseBody
    public  BaseResponse rderV3(@Valid @RequestBody BaseRequest<SeckillReqV3> req){
        CommonWebUser user = WebUserUtil.getLoginUser();
        if (Objects.isNull(user)){
            return BaseResponse.error(ErrorMessage.USER_NEED_LOGIN);
        }
        SeckillReqV3 data = req.getData();
        SeckillUser phoneUser = userService.findAllByPhone(user.getPhone());
        data.setUserId(phoneUser.getId());
        return seckillImageService.orderV3(data);
    }


    @RequestMapping("/code/page")
    public String showCode(){
        return "code";
    }


}
