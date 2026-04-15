package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "经验分享详情DTO")
public class ShareDetailDTO {
  @Schema(description = "主键ID")
  private Long id;

  @Schema(description = "员工号")
  private String empNo;

  @Schema(description = "所属支行")
  private String branchName;

  @Schema(description = "分享主题")
  private String title;

  @Schema(description = "提交时间")
  private LocalDateTime submitTime;

  @Schema(description = "备注")
  private String remark;

  @Schema(description = "审批结果：1=通过，2=驳回（未审批则为空）")
  private Integer auditResult;

  @Schema(description = "审批意见（未审批则为空）")
  private String auditDesc;
}
