package com.example.demo.controller;


import com.example.demo.entity.ExperienceShare;
import com.example.demo.service.ExperienceShareService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "经验分享管理", description = "经验分享提交、审批、查询全流程接口")
@RestController
@RequestMapping("/api/share")
public class ExperienceShareController {

  @Autowired
  private ExperienceShareService shareService;

  @Operation(summary = "提交经验分享", description = "员工提交新的经验分享，初始状态为待审批")
  @PostMapping("/submit")
  public String submit(@RequestBody ExperienceShare share) {
    boolean success = shareService.submitShare(share);
    return success ? "提交成功" : "提交失败";
  }

  @Operation(summary = "审批经验分享", description = "管理员审批经验分享，支持通过/驳回，驳回时需填写原因")
  @PostMapping("/audit")
  public String audit(
      @Parameter(description = "经验分享ID", required = true) @RequestParam Long id,
      @Parameter(description = "审批结果：1=通过，2=驳回", required = true) @RequestParam Integer auditResult,
      @Parameter(description = "审批人姓名", required = true) @RequestParam String auditBy,
      @Parameter(description = "驳回原因（驳回时必填）") @RequestParam(required = false) String rejectReason) {
    boolean success = shareService.auditShare(id, auditResult, auditBy, rejectReason);
    return success ? "审批完成" : "审批失败";
  }

  @Operation(summary = "分页查询经验分享列表", description = "支持按状态筛选（待审批/通过/驳回），默认按提交时间倒序")
  @GetMapping("/page")
  public PageInfo<ExperienceShare> page(
      @Parameter(description = "当前页码，默认1") @RequestParam(defaultValue = "1") Integer current,
      @Parameter(description = "每页条数，默认10") @RequestParam(defaultValue = "10") Integer size,
      @Parameter(description = "状态筛选：0=待审批，1=通过，2=驳回，不传则查询全部") @RequestParam(required = false) Integer status) {
    return shareService.getSharePage(current, size, status);
  }

  @Operation(summary = "获取经验分享详情", description = "根据ID获取单条经验分享的完整信息")
  @GetMapping("/{id}")
  public ExperienceShare detail(
      @Parameter(description = "经验分享ID", required = true) @PathVariable Long id) {
    return shareService.getDetail(id);
  }
}