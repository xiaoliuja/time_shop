package com.xiaoliu.seckill.service;

import com.xiaoliu.seckill.model.SeckillProducts;
import com.xiaoliu.seckill.util.bean.CommonQueryBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductService {

    List<SeckillProducts> page(String name,int pageNum,int pageSize);

    int countProduct();

    Long insertOne(SeckillProducts seckillProducts);

    SeckillProducts  selectByPrimaryKey (Long id);

    int updateByPrimaryKeySelective( SeckillProducts record );

    int deleteByPrimaryKey (Long id );
}
