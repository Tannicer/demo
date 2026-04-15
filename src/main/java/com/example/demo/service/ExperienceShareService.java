package com.example.demo.service;

import com.example.demo.dto.AuditStatsDTO;
import com.example.demo.dto.AuditRecordDTO;
import com.example.demo.entity.ExperienceShare;
import com.example.demo.entity.ExperienceShareAudit;
import com.example.demo.mapper.ExperienceShareAuditMapper;
import com.example.demo.mapper.ExperienceShareMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExperienceShareService {
  @Autowired
  private ExperienceShareMapper shareMapper;
  @Autowired
  private ExperienceShareAuditMapper auditMapper;

  // 提交分享（新增：多选客户数组转逗号字符串）
  public boolean submitShare(ExperienceShare share) {
    share.setStatus(0);
    share.setSubmitTime(LocalDateTime.now());
    share.setCreateTime(LocalDateTime.now());
    share.setIsDeleted(0);

    // ===================== 核心转换 =====================
    // 前端传多选数组 → 转为逗号分隔字符串存库
    if (share.getRelatedCustomerList() != null && !share.getRelatedCustomerList().isEmpty()) {
      String relatedStr = String.join(",", share.getRelatedCustomerList());
      share.setRelatedCustomers(relatedStr);
    }

    return shareMapper.insert(share) > 0;
  }

  // 详情查询（新增：数据库字符串 → 转回数组给前端）
  public ExperienceShare getDetail(Long id) {
    ExperienceShare share = shareMapper.selectById(id);
    if (share == null) return null;

    // ===================== 核心转换 =====================
    // 数据库字符串 → 转回List数组
    if (StringUtils.hasText(share.getRelatedCustomers())) {
      List<String> list = Arrays.asList(share.getRelatedCustomers().split(","));
      share.setRelatedCustomerList(list);
    } else {
      share.setRelatedCustomerList(new ArrayList<>());
    }
    return share;
  }

  // 分页查询（统一转换关联客户字段）
  public PageInfo<ExperienceShare> getSharePage(Integer current, Integer size, Integer status) {
    PageHelper.startPage(current, size);
    List<ExperienceShare> list = shareMapper.selectList(status);

    // 批量转换
    for (ExperienceShare share : list) {
      if (StringUtils.hasText(share.getRelatedCustomers())) {
        share.setRelatedCustomerList(Arrays.asList(share.getRelatedCustomers().split(",")));
      } else {
        share.setRelatedCustomerList(new ArrayList<>());
      }
    }
    return new PageInfo<>(list);
  }

  // ===================== 以下代码保持不变 =====================
  @Transactional(rollbackFor = Exception.class)
  public boolean auditShare(Long id, Integer auditResult, String auditBy, String auditDesc) {
    int update = shareMapper.updateStatus(id, auditResult, LocalDateTime.now());
    if (update == 0) return false;

    ExperienceShareAudit audit = new ExperienceShareAudit();
    audit.setShareId(id);
    audit.setAuditResult(auditResult);
    audit.setAuditBy(auditBy);
    audit.setAuditTime(LocalDateTime.now());
    audit.setCreateTime(LocalDateTime.now());
    audit.setIsDeleted(0);
    audit.setAuditDesc(auditDesc);

    return auditMapper.insertAudit(audit) > 0;
  }

  public List<ExperienceShareAudit> getAuditRecords(Long shareId) {
    return auditMapper.selectByShareId(shareId);
  }

  public AuditStatsDTO getAuditStats(String auditBy) {
    AuditStatsDTO stats = new AuditStatsDTO();
    stats.setPendingCount(shareMapper.getPendingAuditCount() == null ? 0 : shareMapper.getPendingAuditCount());
    stats.setTodayCount(auditMapper.getTodayAuditCount(auditBy) == null ? 0 : auditMapper.getTodayAuditCount(auditBy));
    stats.setWeekCount(auditMapper.getWeekAuditCount(auditBy) == null ? 0 : auditMapper.getWeekAuditCount(auditBy));
    stats.setMonthCount(auditMapper.getMonthAuditCount(auditBy) == null ? 0 : auditMapper.getMonthAuditCount(auditBy));
    return stats;
  }

  public List<AuditRecordDTO> getRecentAuditRecords(String auditBy) {
    List<AuditRecordDTO> result = new ArrayList<>();
    List<ExperienceShareAudit> auditList = auditMapper.getRecentAuditList(auditBy);
    for (ExperienceShareAudit audit : auditList) {
      AuditRecordDTO dto = new AuditRecordDTO();
      dto.setAuditBy(audit.getAuditBy());
      dto.setAuditResult(audit.getAuditResult());
      dto.setAuditDesc(audit.getAuditDesc());
      dto.setAuditTime(audit.getAuditTime());
      ExperienceShare share = shareMapper.selectById(audit.getShareId());
      dto.setShareTitle(share != null ? share.getTitle() : "已删除");
      result.add(dto);
    }
    return result;
  }
}