package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "经验分享主表")
public class ExperienceShare {
  @Schema(description = "主键ID", example = "1")
  private Long id;

  @Schema(description = "分享主题", required = true)
  private String title;

  // 🔥 原有单选字段（保持不变）
  @Schema(description = "分享形式（单选下拉）", required = true)
  private String type;

  @Schema(description = "客群分类（单选下拉）", required = true)
  private String category;

  @Schema(description = "分享摘要")
  private String summary;
  @Schema(description = "分享内容", required = true)
  private String content;
  @Schema(description = "附件地址")
  private String fileUrl;
  @Schema(description = "附件名称")
  private String fileName;
  @Schema(description = "员工号", required = true)
  private String empNo;
  @Schema(description = "所属支行")
  private String branchName;
  @Schema(description = "提交时间")
  private LocalDateTime submitTime;
  @Schema(description = "备注")
  private String remark;
  @Schema(description = "状态：0=待审批，1=通过，2=驳回")
  private Integer status;
  @Schema(description = "创建人")
  private String createBy;
  @Schema(description = "创建时间")
  private LocalDateTime createTime;
  @Schema(description = "更新人")
  private String updateBy;
  @Schema(description = "更新时间")
  private LocalDateTime updateTime;
  @Schema(description = "逻辑删除")
  private Integer isDeleted;

  // ===================== 新增字段 =====================
  // 数据库存储：逗号分隔字符串
  @Schema(hidden = true) // 隐藏，不暴露给前端
  private String relatedCustomers;

  // 前端交互：多选数组（接收/返回）
  @Schema(description = "关联客户（多选下拉）", example = "[\"张三客户\",\"李四客户\"]")
  private List<String> relatedCustomerList;
}