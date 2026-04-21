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

  /**
   * 更新已开户客户的account_status为1
   */
  void updateAccountStatusToOpened();

  /**
   * 查询客户号对应的客户经理ID和客群编码
   */
  List<Map<String, Object>> selectCustomerInfoWithGroupCode();

  /**
   * 查询客户号对应的基础户/有效户状态
   */
  List<Map<String, Object>> selectCustomerDetailStatus();

  /**
   * 查询客户经理的经验分享记录（已提交且审批通过）
   */
  List<Map<String, Object>> selectExperienceShares();

  /**
   * 查询加分规则
   */
  List<Map<String, Object>> selectScoreAddRules();

  /**
   * 计算客户经理的总积分
   */
  BigDecimal calculateTotalScore(String managerId);

  /**
   * 更新客户经理排名
   */
  void updateRanking();

  /**
   * 查询客户产品信息
   */
  List<Map<String, Object>> selectCustomerProducts();

  /**
   * 查询客户经理基础户增量排名
   */
  List<Map<String, Object>> selectBaseCustomerIncrementRank();

  /**
   * 查询客户经理有效户增量排名
   */
  List<Map<String, Object>> selectValidCustomerIncrementRank();

  /**
   * 检查是否存在重复的积分变更记录
   */
  int checkDuplicateScoreChange(@Param("managerId") String managerId, @Param("treasureName") String treasureName, 
                               @Param("customerNo") String customerNo, @Param("shareId") Long shareId);

  /**
   * 查询所有客户经理的积分变更记录（分页）
   */
  List<Map<String, Object>> selectScoreChangeList(@Param("offset") int offset, @Param("size") int size);

  /**
   * 查询单个客户经理的积分变更记录（分页）
   */
  List<Map<String, Object>> selectScoreChangeListByManagerId(@Param("managerId") String managerId, 
                                                            @Param("offset") int offset, @Param("size") int size);

  /**
   * 查询所有积分变更记录的总数
   */
  long countScoreChangeList();

  /**
   * 查询单个客户经理的积分变更记录总数
   */
  long countScoreChangeListByManagerId(@Param("managerId") String managerId);
}
