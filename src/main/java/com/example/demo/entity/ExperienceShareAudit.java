package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "经验分享审核记录表")
public class ExperienceShareAudit {
  @Schema(description = "主键ID")
  private Long id;

  @Schema(description = "关联主表ID", required = true)
  private Long shareId;

  @Schema(description = "审批结果：1=通过，2=驳回")
  private Integer auditResult;

  @Schema(description = "审批时间")
  private LocalDateTime auditTime;

  @Schema(description = "审批人")
  private String auditBy;

  @Schema(description = "审批描述")
  private String auditDesc;

  @Schema(description = "创建时间")
  private LocalDateTime createTime;

  @Schema(description = "逻辑删除")
  private Integer isDeleted;
}
