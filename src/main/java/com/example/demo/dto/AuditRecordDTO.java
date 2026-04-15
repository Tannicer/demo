package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "审批记录详情")
public class AuditRecordDTO {
  @Schema(description = "审批人")
  private String auditBy;

  @Schema(description = "审批结果 1=通过 2=驳回")
  private Integer auditResult;

  @Schema(description = "经验分享名称")
  private String shareTitle;

  @Schema(description = "审批描述")
  private String auditDesc;

  @Schema(description = "审批时间")
  private LocalDateTime auditTime;
}
