package com.xiaoliu.seckill.controller;

import com.xiaoliu.seckill.model.SeckillAdmin;
import com.xiaoliu.seckill.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;


    @RequestMapping("/listAdminPage")
    public String listAdmin(Model model){

        SeckillAdmin seckillAdmin = new SeckillAdmin();
        List<SeckillAdmin> list = adminService.list(seckillAdmin);

        model.addAttribute("list", list);
        return "admin/listAdminPage";
    }
}
