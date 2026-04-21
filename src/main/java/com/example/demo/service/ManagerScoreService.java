package com.example.demo.service;

import com.example.demo.dto.PageResponse;
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
   * 获取单位（支行）排行榜
   */
  List<Map<String, Object>> getBranchRankList();

  /**
   * 获取段位排行榜（人员）
   */
  List<Map<String, Object>> getLevelRankList();

  /**
   * 获取各段位人数统计
   */
  List<Map<String, Object>> getLevelCount();

  /**
   * 获取所有客户经理的积分变更记录（分页）
   */
  PageResponse<Map<String, Object>> getScoreChangeList(int page, int size);

  /**
   * 获取单个客户经理的积分变更记录（分页）
   */
  PageResponse<Map<String, Object>> getScoreChangeListByManagerId(String managerId, int page, int size);
}
