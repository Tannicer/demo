package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客群分类表（含对应积分）
 */
@Data
public class CustomerGroupCategory {
  /**
   * 主键ID
   */
  private Long id;

  /**
   * 客群分类编码（唯一）
   */
  private String groupCode;

  /**
   * 客群分类名称
   */
  private String groupName;

  /**
   * 对应积分（可变）
   */
  private Integer score;

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
