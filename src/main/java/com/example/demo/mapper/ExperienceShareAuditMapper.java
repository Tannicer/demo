package com.example.demo.mapper;

import com.example.demo.entity.ExperienceShareAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExperienceShareAuditMapper {
  int insertAudit(ExperienceShareAudit audit);
  List<ExperienceShareAudit> selectByShareId(@Param("shareId") Long shareId);

  // 按审批人统计
  Integer getTodayAuditCount(@Param("auditBy") String auditBy);
  Integer getWeekAuditCount(@Param("auditBy") String auditBy);
  Integer getMonthAuditCount(@Param("auditBy") String auditBy);

  // 🔥 单表查询：指定审批人最近3条审核记录（无联表）
  List<ExperienceShareAudit> getRecentAuditList(@Param("auditBy") String auditBy);
}