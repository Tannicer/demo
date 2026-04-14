package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "经验分享实体类")
public class ExperienceShare {

  @Schema(description = "主键ID", example = "1")
  private Long id;

  @Schema(description = "分享主题", required = true, example = "中小企业拓客心得分享")
  private String title;

  @Schema(description = "分享形式", required = true, example = "经验心得")
  private String type;

  @Schema(description = "所属客群分类", required = true, example = "中小企业客群")
  private String category;

  @Schema(description = "分享摘要（200字内）", example = "总结中小企业拓客的五大关键策略")
  private String summary;

  @Schema(description = "详细分享内容", required = true, example = "1. 精准客群定位 2. 定制化金融方案 3. 后续跟进维护")
  private String content;

  @Schema(description = "附件地址", example = "https://example.com/files/share1.pdf")
  private String fileUrl;

  @Schema(description = "附件名称", example = "拓客心得分享.pdf")
  private String fileName;

  @Schema(description = "员工号", required = true, example = "17001")
  private String empNo;

  @Schema(description = "所属支行", example = "天河支行")
  private String branchName;

  @Schema(description = "提交时间", example = "2026-04-02T10:30:00")
  private LocalDateTime submitTime;

  @Schema(description = "备注", example = "总结2026年第一季度拓客经验")
  private String remark;

  @Schema(description = "状态：0=待审批，1=通过，2=驳回", example = "0")
  private Integer status;

  @Schema(description = "审批结果：1=通过，2=驳回", example = "1")
  private Integer auditResult;

  @Schema(description = "审批时间", example = "2026-04-02T11:00:00")
  private LocalDateTime auditTime;

  @Schema(description = "审批人", example = "管理员")
  private String auditBy;

  @Schema(description = "驳回原因", example = "内容质量不高，无实际参考价值")
  private String rejectReason;

  @Schema(description = "创建人", example = "张三")
  private String createBy;

  @Schema(description = "创建时间", example = "2026-04-02T10:30:00")
  private LocalDateTime createTime;

  @Schema(description = "更新人", example = "管理员")
  private String updateBy;

  @Schema(description = "更新时间", example = "2026-04-02T11:00:00")
  private LocalDateTime updateTime;

  @Schema(description = "逻辑删除：0=正常，1=删除", example = "0")
  private Integer isDeleted;
}
