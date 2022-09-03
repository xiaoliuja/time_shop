package com.xiaoliu.seckill.base;



import javax.validation.constraints.NotNull;

import java.io.Serializable;

public class PageReq implements Serializable {


    @NotNull(message = "page 不能为空")
    private Integer page;

    @NotNull(message = "pageSize 不能为空")
    private Integer pageSize;


    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
