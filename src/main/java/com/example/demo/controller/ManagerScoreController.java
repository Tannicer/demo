package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.service.ManagerScoreService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/score")
public class ManagerScoreController {

  @Autowired
  private ManagerScoreService managerScoreService;

  /**
   * 手动刷新所有客户经理积分
   * @return 刷新结果
   */
  @GetMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refreshScore() {
    managerScoreService.calculateAllManagerScore();
    Map<String, Object> result = new HashMap<>();
    result.put("code", 200);
    result.put("message", "积分计算成功！");
    return ResponseEntity.ok(result);
  }

  /**
   * 【新增】获取客户经理积分排行榜
   * 返回格式：排名、客户经理ID、姓名、支行、总积分、段位
   */
  @GetMapping("/rank")
  public ResponseEntity<List<Map<String, Object>>> getRankList() {
    List<Map<String, Object>> rankList = managerScoreService.getManagerRankList();
    return ResponseEntity.ok(rankList);
  }



  /**
   * 获取单位（支行）排行榜
   */
  @GetMapping("/branch-rank")
  public ResponseEntity<List<Map<String, Object>>> getBranchRank() {
    return ResponseEntity.ok(managerScoreService.getBranchRankList());
  }

  /**
   * 获取段位排行榜（人员）
   */
  @GetMapping("/level-rank")
  public ResponseEntity<List<Map<String, Object>>> levelRank() {
    return ResponseEntity.ok(managerScoreService.getLevelRankList());
  }

  /**
   * 获取各段位人数统计
   */
  @GetMapping("/level-count")
  public ResponseEntity<List<Map<String, Object>>> levelCount() {
    return ResponseEntity.ok(managerScoreService.getLevelCount());
  }

  /**
   * 获取所有客户经理的积分变更记录（分页）
   */
  @PostMapping("/change/list")
  public ResponseEntity<PageResponse<Map<String, Object>>> getScoreChangeList(
      @RequestParam(defaultValue = "1") int page, 
      @RequestParam(defaultValue = "10") int size) {
    PageResponse<Map<String, Object>> response = managerScoreService.getScoreChangeList(page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * 获取单个客户经理的积分变更记录（分页）
   */
  @PostMapping("/change/list-by-manager")
  public ResponseEntity<PageResponse<Map<String, Object>>> getScoreChangeListByManagerId(
      @RequestParam String managerId,
      @RequestParam(defaultValue = "1") int page, 
      @RequestParam(defaultValue = "10") int size) {
    PageResponse<Map<String, Object>> response = managerScoreService.getScoreChangeListByManagerId(managerId, page, size);
    return ResponseEntity.ok(response);
  }
}
