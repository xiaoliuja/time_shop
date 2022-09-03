package com.xiaoliu.seckill.controller;

import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.service.ProductService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.xiaoliu.seckill.service.ProductService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/product")
public class ProductController {


    @Autowired
    private ProductService productService;

    /*@RequestMapping("/main")
    public String main(){
        return "product/listPageSeckillProducts";
    }
*/

    @RequestMapping("/listPage")
    //分页   内部要加 根据 name查询分页
    public String listPageProducts(Model model, String name,
                                   @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "pageSize",defaultValue = "2") Integer pageSize){


        if(StringUtils.isNotEmpty(name)){
            //将name传给前端， 让前端获取到name的值
            model.addAttribute("name", name);
        }
        List<SeckillProducts> list = productService.page(name, pageNum, pageSize);



        int total = productService.countProduct();
        int pageTotal = total/pageSize;
        if (total%pageSize > 0){
            pageTotal ++;
        }

        model.addAttribute("list",list);
        model.addAttribute("total", total);
        model.addAttribute("totalPage", pageTotal);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNum", pageNum);


        return "product/listPageSeckillProducts";
    }



    //新增页面
    @RequestMapping("/beforeCreateProduct")
    public String insertPage(){
        return "product/beforeCreateProduct";
    }


    //新增数据
    //由于jsp页面写的日期为 String类型的，实在想不到转换为Date对象的方法，所以只能后端接口传String，然后获取到String变量解析为 Date类型的。
    @PostMapping("/insertProducts")
    public String doinsertProduct(String startBuyTime,String name,Integer count,String productPeriodKey,String productDesc){

        Assert.notNull(name, "商品名称不能为空！！");
        Assert.notNull(count, "库存数量不能为空！！");
        Assert.notNull(startBuyTime, "秒杀开始不能为空！！");
        Assert.notNull(productPeriodKey, "唯一标识不能为空！！");
        Assert.notNull(productDesc, "商品简介不能为空！！");

        SeckillProducts seckillProducts = new SeckillProducts();
        seckillProducts.setName(name);
        seckillProducts.setCount(count);
        seckillProducts.setProductPeriodKey(productPeriodKey);
        seckillProducts.setProductDesc(productDesc);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(startBuyTime);
            seckillProducts.setStartBuyTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //新增唯一
        productService.insertOne(seckillProducts);

        return "redirect:listPage?isSave=yes";
    }



    //更新页面 要把当前更新的product的信息给传过去
    @RequestMapping("/beforeUpdateProduct")
    public String updatePage(Model model,Long id){

        SeckillProducts seckillProducts = productService.selectByPrimaryKey(id);

        if (seckillProducts != null) {
            model.addAttribute("item", seckillProducts);

        }
        return "product/beforeUpdateProduct";
    }
    //与新增类似由于jsp页面的开始时间为String类型的，所以不能传对象，String让传过去的对象接受Strinig类型的date属实不会呃呃。。
    @PostMapping("/doUpdate")
    public String doUpdateProduct(Long id, String name,
                                  String startBuyTime, Integer amount, String desc){

        Assert.notNull(name, "商品名称不能为空！！");
        Assert.notNull(amount, "库存数量不能为空！！");
        Assert.notNull(startBuyTime, "秒杀开始不能为空！！");
        Assert.notNull(desc, "商品简介不能为空！！");

        //写的时候后来脑残了，md创了个新得对象去赋值然后更新，真是个sb，不用id查询该对象，创个新的对象人知道你更新的哪个对象？？
        SeckillProducts seckillProducts = productService.selectByPrimaryKey(id);


        seckillProducts.setUpdatedTime(new Date());
        seckillProducts.setProductDesc(desc);
        seckillProducts.setName(name);
        seckillProducts.setCount(amount);


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(startBuyTime);
            seckillProducts.setStartBuyTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        productService.updateByPrimaryKeySelective(seckillProducts);
        return "redirect:listPage";
    }


    // 页面查看按键  展示该product的元素信息，详情页面中所有元素都展示
    @RequestMapping("/showProductItem")
    public String showProductItem(Model model,Long id){

        SeckillProducts seckillProducts = productService.selectByPrimaryKey(id);
        if (seckillProducts != null){
            model.addAttribute("item", seckillProducts);
        }
        return "product/showProductItem";
    }


    //页面有一个上架下架按钮，可以更新 商品的 status状态
   @RequestMapping("/updateProductStatus")
    public String updateProductStatus(Long id, Integer status){
        //查出该商品的信息来，再把状态一改
       SeckillProducts seckillProducts = productService.selectByPrimaryKey(id);
       seckillProducts.setStatus(status);

       //模拟逻辑删除 当这个项目上架的时候，如果该商品逻辑删除过了并且要修改为上架状态，就把它的逻辑删除改为0
       int is_deleted = seckillProducts.getIsDeleted();


       //当点击项目上架的时候，商品的删除状态一定为正常的  is_deleted=0;
       if (status == 1 && is_deleted == 1 ){
           seckillProducts.setIsDeleted(0);
       }

       seckillProducts.setUpdatedTime(new Date());

       productService.updateByPrimaryKeySelective(seckillProducts);

       return "redirect:listPage";
   }



   //逻辑删除
    @RequestMapping("/deleteProduct")
    public String deleteProduct(Long id){
        SeckillProducts seckillProducts = productService.selectByPrimaryKey(id);
        seckillProducts.setIsDeleted(1);
        seckillProducts.setUpdatedTime(new Date());
        //为逻辑删除所以这里为修改操作
        productService.updateByPrimaryKeySelective(seckillProducts);
        return "redirect:listPage";
    }

}
