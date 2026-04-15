package com.example.demo.service;

import com.example.demo.dto.AuditStatsDTO;
import com.example.demo.dto.AuditRecordDTO;
import com.example.demo.dto.ShareDetailDTO;
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
// 【变更2】修复提交时间赋值
  public boolean submitShare(ExperienceShare share) {
    if (share.getTitle() == null || share.getTitle().trim().isEmpty()
        || share.getType() == null || share.getType().trim().isEmpty()
        || share.getCategory() == null || share.getCategory().trim().isEmpty()
        || share.getContent() == null || share.getContent().trim().isEmpty()
        || share.getEmpNo() == null || share.getEmpNo().trim().isEmpty()) {
      return false;
    }
    if (share.getRelatedCustomerList() != null && !share.getRelatedCustomerList().isEmpty()) {
      share.setRelatedCustomers(String.join(",", share.getRelatedCustomerList()));
    }
    share.setIsDeleted(0);
    share.setCreateTime(LocalDateTime.now());
    if (share.getDraftStatus() == 1) {
      share.setSubmitTime(LocalDateTime.now());
    }

    if (share.getDraftStatus() == null) {
      share.setDraftStatus(0);
    }
    if (share.getDraftStatus() == 1) {
      share.setStatus(0);
    } else {
      share.setStatus(null);
    }

    return shareMapper.insert(share) > 0;
  }

  // ===================== 查询我的草稿 =====================
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

  // 【变更1】详情查询方法（无联表、仅核心字段）
  public ShareDetailDTO getDetail(Long id) {
    // 1. 单表查询主表数据
    ExperienceShare share = shareMapper.selectById(id);
    if (share == null) return null;

    // 2. 构建详情DTO
    ShareDetailDTO detailDTO = new ShareDetailDTO();
    detailDTO.setId(share.getId());
    detailDTO.setEmpNo(share.getEmpNo());
    detailDTO.setBranchName(share.getBranchName());
    detailDTO.setTitle(share.getTitle());
    detailDTO.setSubmitTime(share.getSubmitTime());
    detailDTO.setRemark(share.getRemark());

    // 3. 已审批时单表查询审批记录
    if (share.getStatus() != null && (share.getStatus() == 1 || share.getStatus() == 2)) {
      List<ExperienceShareAudit> auditList = auditMapper.selectByShareId(share.getId());
      if (!auditList.isEmpty()) {
        ExperienceShareAudit latestAudit = auditList.stream()
            .sorted((a1, a2) -> a2.getAuditTime().compareTo(a1.getAuditTime()))
            .findFirst()
            .orElse(null);
        if (latestAudit != null) {
          detailDTO.setAuditResult(latestAudit.getAuditResult());
          detailDTO.setAuditDesc(latestAudit.getAuditDesc());
        }
      }
    }
    return detailDTO;
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

  // ===================== （审批/统计/记录） =====================
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