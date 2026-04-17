package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 段位信息表
 */
@Data
public class LevelInfo {
  /**
   * 主键ID
   */
  private Long id;

  /**
   * 段位编码（唯一）
   */
  private String levelCode;

  /**
   * 段位名称
   */
  private String levelName;

  /**
   * 最低积分
   */
  private Integer minScore;

  /**
   * 最高积分（null表示无上限）
   */
  private Integer maxScore;

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
