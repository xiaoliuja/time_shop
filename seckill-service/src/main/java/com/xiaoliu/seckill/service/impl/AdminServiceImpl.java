package com.xiaoliu.seckill.service.impl;

import com.xiaoliu.seckill.dao.SeckillAdminDao;
import com.xiaoliu.seckill.model.SeckillAdmin;
import com.xiaoliu.seckill.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private SeckillAdminDao seckillAdminDao;

    @Override
    public List<SeckillAdmin> list(SeckillAdmin record) {
        return seckillAdminDao.list(record);
    }
}
