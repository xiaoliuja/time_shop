package com.xiaoliu.seckill.service.impl;

import com.xiaoliu.seckill.dao.SeckillProductsDao;
import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.service.ProductService;
import com.xiaoliu.seckill.util.bean.CommonQueryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private SeckillProductsDao productsDao;


    //分页
    public List<SeckillProducts> page(String name,int pageNum,int pageSize){

        SeckillProducts seckillProducts = new SeckillProducts();

        if(StringUtils.isNotEmpty(name)){
            //如果传了name值，就根据查询的 name字段分页
            seckillProducts.setName(name);
        }
        CommonQueryBean commonQueryBean = new CommonQueryBean();
        int start = (pageNum-1) * pageSize;
        commonQueryBean.setStart(start);
        commonQueryBean.setPageSize(pageSize);


        return productsDao.list4Page(seckillProducts,commonQueryBean);
    }

    @Override
    public int countProduct() {
        SeckillProducts records = new SeckillProducts();
        return productsDao.count(records);
    }

    //添加唯一索引实现新增唯一
    @Override
    public Long insertOne(SeckillProducts seckillProducts) {
        //通过数据库的唯一标识 product_period_key来判断新增唯一 给它设置唯一索引
        seckillProducts.setCreateTime(new Date());
        seckillProducts.setIsDeleted(0);
        seckillProducts.setStatus(2);


        try {
            int insert = productsDao.insert(seckillProducts);
        } catch (Exception e) {
            if (e.getMessage().indexOf("DUplicate entry") >= 0){
                //如果报的异常是重复不唯一插入异常的话
                //返回这个已经注册的id
                SeckillProducts products = productsDao.findByPeriodKey(seckillProducts.getProductPeriodKey());
                return products.getId();
            }else {
                log.info("异常为{}",e);
            }
        }

        return seckillProducts.getId();

    }

    @Override
    public SeckillProducts selectByPrimaryKey(Long id) {
        return productsDao.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(SeckillProducts record) {
        return productsDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productsDao.deleteByPrimaryKey(id);
    }


}
