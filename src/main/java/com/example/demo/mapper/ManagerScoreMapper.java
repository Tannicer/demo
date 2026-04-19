package com.example.demo.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 客户经理积分计算 Mapper
 */
@Mapper
public interface ManagerScoreMapper {

  /**
   * 第一步：查询已开户客户 → 客户经理【最高客群基础积分】
   */
  List<Map<String, Object>> selectMaxGroupScore(@Param("accountStatus") String accountStatus);

  /**
   * 第二步：查询已开户客户 → 产品奖励积分（封顶6分）
   */
  List<Map<String, Object>> selectProductScore(@Param("accountStatus") String accountStatus);

  /**
   * 第三步：查询审批通过的经验分享 → 分享积分（1个+30分）
   */
  List<Map<String, Object>> selectShareScore(@Param("approveStatus") Integer approveStatus);

  /**
   * 根据总积分匹配段位编码
   */
  String selectLevelCodeByScore(@Param("totalScore") BigDecimal totalScore);

  /**
   * 更新客户经理总积分、段位
   */
  void updateManagerScore(Map<String, Object> param);

  void updateManagerRank(Map<String, Object> param);

  /**
   * 查询客户经理积分排行榜（按排名正序）
   */
  List<Map<String, Object>> selectManagerRankList();

  // 查询单个客户经理 客群基础积分
  BigDecimal selectSingleBaseScore(@Param("managerId") String managerId, @Param("accountStatus") String accountStatus);

  // 查询单个客户经理 产品积分
  BigDecimal selectSingleProductScore(@Param("managerId") String managerId, @Param("accountStatus") String accountStatus);

  // 查询单个客户经理 经验分享积分
  BigDecimal selectSingleShareScore(@Param("managerId") String managerId, @Param("approveStatus") Integer approveStatus);

  // 查询客户经理基础信息+段位
  Map<String, Object> selectManagerDetail(@Param("managerId") String managerId);

  // 单位（支行）排行榜
  List<Map<String, Object>> selectBranchRankList();

  // 段位排行榜（所有人按积分倒序，带段位、单位、积分）
  List<Map<String, Object>> selectLevelRankList();

  // 各段位人数统计
  List<Map<String, Object>> selectLevelCount();

  List<Map<String, Object>> statCustomerCount();

  BigDecimal getAddScore(String type);

  Map<String, Object> getDeductRule();
}
