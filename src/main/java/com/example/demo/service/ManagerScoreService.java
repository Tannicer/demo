package com.example.demo.service;

import java.util.List;
import java.util.Map;

/**
 * 客户经理积分业务接口
 */
public interface ManagerScoreService {

  /**
   * 【核心入口】批量计算并更新所有客户经理的总积分、段位、排名
   */
  void calculateAllManagerScore();

  /**
   * 获取客户经理积分排行榜
   */
  List<Map<String, Object>> getManagerRankList();

  /**
   * 根据客户经理ID查询积分明细
   */
  Map<String, Object> getManagerScoreDetail(String managerId);

  List<Map<String, Object>> getBranchRankList();

  // 段位排行榜（人员）
  List<Map<String, Object>> getLevelRankList();

  // 各段位人数统计
  List<Map<String, Object>> getLevelCount();
}
