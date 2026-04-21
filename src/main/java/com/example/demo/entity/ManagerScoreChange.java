package com.example.demo.entity;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
  // 客户号（18位）
  private String customerNo;
  // 客户名称
  private String customerName;
  // 分享ID
  private Long shareId;
  // 分享标题
  private String shareTitle;
  // 删除标识
  private Integer isDeleted;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTreasureName() {
    return treasureName;
  }

  public void setTreasureName(String treasureName) {
    this.treasureName = treasureName;
  }

  public String getTreasureDesc() {
    return treasureDesc;
  }

  public void setTreasureDesc(String treasureDesc) {
    this.treasureDesc = treasureDesc;
  }

  public String getManagerId() {
    return managerId;
  }

  public void setManagerId(String managerId) {
    this.managerId = managerId;
  }

  public String getManagerName() {
    return managerName;
  }

  public void setManagerName(String managerName) {
    this.managerName = managerName;
  }

  public String getPost() {
    return post;
  }

  public void setPost(String post) {
    this.post = post;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public BigDecimal getScore() {
    return score;
  }

  public void setScore(BigDecimal score) {
    this.score = score;
  }

  public LocalDateTime getTriggerTime() {
    return triggerTime;
  }

  public void setTriggerTime(LocalDateTime triggerTime) {
    this.triggerTime = triggerTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getCustomerNo() {
    return customerNo;
  }

  public void setCustomerNo(String customerNo) {
    this.customerNo = customerNo;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public Long getShareId() {
    return shareId;
  }

  public void setShareId(Long shareId) {
    this.shareId = shareId;
  }

  public String getShareTitle() {
    return shareTitle;
  }

  public void setShareTitle(String shareTitle) {
    this.shareTitle = shareTitle;
  }

  public Integer getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Integer isDeleted) {
    this.isDeleted = isDeleted;
  }
}
