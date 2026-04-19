package com.example.demo.entity;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ManagerScoreChange {
  private Long id;
  // 锦囊名称
  private String treasureName;
  // 锦囊描述
  private String treasureDesc;
  // 客户经理ID/行员号
  private String managerId;
  // 客户经理姓名
  private String managerName;
  // 职位
  private String post;
  // 所属支行名称
  private String branchName;
  // 积分变动值（+5 / -3）
  private BigDecimal score;
  // 锦囊触发时间
  private LocalDateTime triggerTime;
  // 状态：通过/驳回
  private String status;
  // 备注
  private String remark;
  // 删除标识
  private Integer isDeleted;
}
