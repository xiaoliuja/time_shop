package com.xiaoliu.seckill.model;

import java.util.Date;


/**
 * 
 * 用户表
 * 
 **/
public class SeckillUser{


  /**主键**/

  private Long id;


  /**用户名称**/

  private String name;


  /**用户手机号**/

  private String phone;


  /**创建时间**/

  private Date createTime;


  /**是否删除：0否 1是**/

  private Integer isDeleted;




  public void setId(Long id) { 
  }


  public Long getId() { 
  }


  public void setName(String name) { 
  }


  public String getName() { 
  }


  public void setPhone(String phone) { 
  }


  public String getPhone() { 
  }


  public void setCreateTime(Date createTime) { 
  }


  public Date getCreateTime() { 
  }


  public void setIsDeleted(Integer isDeleted) { 
  }


  public Integer getIsDeleted() { 
  }

}