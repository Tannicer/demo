package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审批统计数据")
public class AuditStatsDTO {
  @Schema(description = "待审批数量")
  private Integer pendingCount;

  @Schema(description = "今日审批数量")
  private Integer todayCount;

  @Schema(description = "本周审批数量")
  private Integer weekCount;

  @Schema(description = "本月审批数量")
  private Integer monthCount;
}
