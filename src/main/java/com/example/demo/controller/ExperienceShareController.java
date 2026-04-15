package com.example.demo.controller;

import com.example.demo.dto.AuditStatsDTO;
import com.example.demo.dto.AuditRecordDTO;
import com.example.demo.entity.ExperienceShare;
import com.example.demo.entity.ExperienceShareAudit;
import com.example.demo.service.ExperienceShareService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "经验分享管理")
@RestController
@RequestMapping("/api/share")
@CrossOrigin
public class ExperienceShareController {
  @Autowired
  private ExperienceShareService shareService;

  private ResponseEntity<Map<String, Object>> result(boolean success, String msg, Object data) {
    Map<String, Object> map = new HashMap<>();
    map.put("success", success);
    map.put("msg", msg);
    map.put("data", data);
    return ResponseEntity.ok(map);
  }

  // 提交
  @PostMapping("/submit")
  public ResponseEntity<Map<String, Object>> submit(@RequestBody ExperienceShare share) {
    if (share.getTitle() == null || share.getTitle().trim().isEmpty()
        || share.getType() == null || share.getType().trim().isEmpty()
        || share.getCategory() == null || share.getCategory().trim().isEmpty()
        || share.getContent() == null || share.getContent().trim().isEmpty()
        || share.getEmpNo() == null || share.getEmpNo().trim().isEmpty()) {
      return result(false, "提交失败：标题/类型/分类/内容/员工号不能为空", null);
    }
    boolean b = shareService.submitShare(share);
    return result(b, b ? "提交成功" : "提交失败", null);
  }

  // 审批
  @PostMapping("/audit")
  public ResponseEntity<Map<String, Object>> audit(
      @RequestParam Long id,
      @RequestParam Integer auditResult,
      @RequestParam String auditBy,
      @RequestParam(required = false) String auditDesc) {
    if (auditResult != 1 && auditResult != 2) {
      return result(false, "审批结果只能是1(通过)或2(驳回)", null);
    }
    boolean b = shareService.auditShare(id, auditResult, auditBy, auditDesc);
    return result(b, b ? "审批成功" : "审批失败", null);
  }

  // 分页
  @GetMapping("/page")
  public ResponseEntity<Map<String, Object>> page(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Integer status) {
    if (current < 1 || size < 1 || size > 100) {
      return result(false, "页码/条数不合法", null);
    }
    return result(true, "查询成功", shareService.getSharePage(current, size, status));
  }

  // 详情
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
    return result(true, "查询成功", shareService.getDetail(id));
  }

  // 审批记录
  @Operation(summary = "查询审批记录")
  @GetMapping("/audit/{shareId}")
  public ResponseEntity<Map<String, Object>> auditRecords(@Parameter(required = true) @PathVariable Long shareId) {
    List<ExperienceShareAudit> list = shareService.getAuditRecords(shareId);
    return result(true, "查询成功", list);
  }

  // 按审批人查询统计数据
  @Operation(summary = "获取审批统计数据", description = "根据审批人查询：待审批/今日/本周/本月审批数量")
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getAuditStats(
      @Parameter(description = "审批人姓名", required = true, example = "管理员")
      @RequestParam String auditBy) {
    AuditStatsDTO stats = shareService.getAuditStats(auditBy);
    return result(true, "查询成功", stats);
  }

  // 🔥 新增：查询指定审批人最近3条审批记录
  @Operation(summary = "查询审批人最近审批记录", description = "返回指定审批人最近3条审批记录，按时间倒序")
  @GetMapping("/recent-audit")
  public ResponseEntity<Map<String, Object>> getRecentAuditRecords(
      @Parameter(description = "审批人姓名", required = true, example = "管理员")
      @RequestParam String auditBy) {
    List<AuditRecordDTO> list = shareService.getRecentAuditRecords(auditBy);
    return result(true, "查询成功", list);
  }
  // 新增：查询我的草稿
  @Operation(summary = "查询我的草稿", description = "查询当前用户暂存的经验分享草稿")
  @GetMapping("/my-draft")
  public ResponseEntity<Map<String, Object>> getMyDraft(
      @RequestParam String createBy,
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size) {
    return result(true, "查询成功", shareService.getMyDraft(createBy, current, size));
  }

}