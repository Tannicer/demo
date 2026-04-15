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

  // ===================== 核心：保存/暂存经验分享 =====================
  public boolean submitShare(ExperienceShare share) {
    // 数据转换：多选客户数组 → 逗号字符串
    if (share.getRelatedCustomerList() != null && !share.getRelatedCustomerList().isEmpty()) {
      share.setRelatedCustomers(String.join(",", share.getRelatedCustomerList()));
    }
    share.setIsDeleted(0);
    share.setCreateTime(LocalDateTime.now());

    // 状态规则：
    // 0=草稿 → 不设置审批状态
    // 1=正式 → 进入待审批(status=0)
    if (share.getDraftStatus() == null) {
      share.setDraftStatus(0); // 默认草稿
    }
    if (share.getDraftStatus() == 1) {
      share.setStatus(0); // 正式提交 → 待审批
    } else {
      share.setStatus(null); // 草稿 → 清空审批状态
    }

    return shareMapper.insert(share) > 0;
  }

  // ===================== 新增：查询我的草稿 =====================
  public PageInfo<ExperienceShare> getMyDraft(String createBy, Integer current, Integer size) {
    PageHelper.startPage(current, size);
    List<ExperienceShare> list = shareMapper.selectMyDraft(createBy);
    // 转换关联客户字段
    for (ExperienceShare share : list) {
      if (StringUtils.hasText(share.getRelatedCustomers())) {
        share.setRelatedCustomerList(Arrays.asList(share.getRelatedCustomers().split(",")));
      } else {
        share.setRelatedCustomerList(new ArrayList<>());
      }
    }
    return new PageInfo<>(list);
  }

  // 详情查询（兼容草稿）
  public ExperienceShare getDetail(Long id) {
    ExperienceShare share = shareMapper.selectById(id);
    if (share == null) return null;
    // 转换关联客户
    if (StringUtils.hasText(share.getRelatedCustomers())) {
      share.setRelatedCustomerList(Arrays.asList(share.getRelatedCustomers().split(",")));
    } else {
      share.setRelatedCustomerList(new ArrayList<>());
    }
    return share;
  }

  // 分页查询（仅正式数据）
  public PageInfo<ExperienceShare> getSharePage(Integer current, Integer size, Integer status) {
    PageHelper.startPage(current, size);
    List<ExperienceShare> list = shareMapper.selectList(status);
    for (ExperienceShare share : list) {
      if (StringUtils.hasText(share.getRelatedCustomers())) {
        share.setRelatedCustomerList(Arrays.asList(share.getRelatedCustomers().split(",")));
      } else {
        share.setRelatedCustomerList(new ArrayList<>());
      }
    }
    return new PageInfo<>(list);
  }

  // ===================== 以下代码保持不变（审批/统计/记录） =====================
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