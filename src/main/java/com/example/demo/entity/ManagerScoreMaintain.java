package com.example.demo.entity;

import java.math.BigDecimal;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户经理积分维护表
 */
@Data
public class ManagerScoreMaintain {
  /**
   * 主键ID
   */
  private Long id;

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
   * 累计积分
   */
  private BigDecimal totalScore;

  /**
   * 段位编码（关联段位信息表）
   */
  private String levelCode;

  /**
   * 排名（动态更新）
   */
  private Integer rankNum;

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