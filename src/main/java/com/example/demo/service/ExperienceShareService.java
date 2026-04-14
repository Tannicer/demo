package com.example.demo.service;


import com.example.demo.entity.ExperienceShare;
import com.example.demo.mapper.ExperienceShareMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ExperienceShareService {

  @Autowired
  private ExperienceShareMapper shareMapper;

  // 1. 提交经验分享
  public boolean submitShare(ExperienceShare share) {
    share.setStatus(0); // 初始状态：待审批
    share.setSubmitTime(LocalDateTime.now());
    share.setCreateTime(LocalDateTime.now());
    return shareMapper.insert(share) > 0;
  }

  // 2. 审批通过/驳回
  public boolean auditShare(Long id, Integer auditResult, String auditBy, String rejectReason) {
    ExperienceShare share = new ExperienceShare();
    share.setId(id);
    share.setAuditResult(auditResult);
    share.setStatus(auditResult); // 同步状态
    share.setAuditBy(auditBy);
    share.setAuditTime(LocalDateTime.now());
    share.setUpdateTime(LocalDateTime.now());

    if (auditResult == 2) {
      share.setRejectReason(rejectReason);
    }
    return shareMapper.updateAudit(share) > 0;
  }

  // 3. 分页查询
  public PageInfo<ExperienceShare> getSharePage(Integer current, Integer size, Integer status) {
    // 开启分页
    PageHelper.startPage(current, size);
    // 执行查询
    List<ExperienceShare> list = shareMapper.selectList(status);
    // 封装分页信息
    return new PageInfo<>(list);
  }

  // 4. 获取详情
  public ExperienceShare getDetail(Long id) {
    return shareMapper.selectById(id);
  }
}
