package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户信息表
 */
@Data
public class CustomerInfo {
  /**
   * 主键ID
   */
  private Long id;

   /**
   * 客户号（18位）
   */
  private String customerNo;

  /**
   * 统一信用代码（18位）
   */
  private String creditCode;

  /**
   * 客户名称
   */
  private String customerName;

  /**
   * 开户状态：0=未开户，1=已开户，2=销户
   */
  private String accountStatus;

  /**
   * 客群分类编码（关联客群分类表）
   */
  private String groupCode;

  /**
   * 产品名称
   */
  private String product;

  /**
   * 客户经理ID（8位数）
   */
  private String managerId;

  /**
   * 客户经理名字
   */
  private String managerName;

  /**
   * 所属支行
   */
  private String branchName;

  /**
   * 逻辑删除：0=未删除，1=已删除
   */
  private Integer isDeleted;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}
