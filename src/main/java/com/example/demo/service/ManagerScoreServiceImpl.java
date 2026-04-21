package com.example.demo.service;
import com.example.demo.entity.ManagerScoreChange;
import com.example.demo.mapper.ExperienceShareMapper;
import com.example.demo.mapper.ManagerScoreChangeMapper;
import com.example.demo.mapper.ManagerScoreMapper;
import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerScoreServiceImpl implements ManagerScoreService {

  @Autowired
  private ManagerScoreMapper managerScoreMapper;

  // 注入Mapper
  @Resource
  private ManagerScoreChangeMapper managerScoreChangeMapper;

  // ========== 新增注入 ==========

  @Resource
  private ExperienceShareMapper experienceShareMapper; // 经验分享Mapper


  // ===================== 固定常量 =====================
  private static final String ACCOUNT_STATUS_OPEN = "1";
  private static final Integer SHARE_STATUS_PASS = 1;
  private static final BigDecimal ZERO = new BigDecimal("0.00");

  // ===================== 核心入口方法 =====================
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void calculateAllManagerScore() {
    log.info("===== 开始执行客户经理积分全量计算（保留2位小数） =====");

    // 1. 分步查询所有积分数据
    List<Map<String, Object>> baseScoreList = managerScoreMapper.selectMaxGroupScore(ACCOUNT_STATUS_OPEN);
    List<Map<String, Object>> productScoreList = managerScoreMapper.selectProductScore(ACCOUNT_STATUS_OPEN);
    List<Map<String, Object>> shareScoreList = managerScoreMapper.selectShareScore(SHARE_STATUS_PASS);

    // ==================== 客户类积分（基础户+有效户） ====================
    List<Map<String, Object>> customerStatList = managerScoreMapper.statCustomerCount();
    BigDecimal basePoint = managerScoreMapper.getAddScore("base");
    BigDecimal validPoint = managerScoreMapper.getAddScore("valid");
    Map<String, Object> ruleMap = managerScoreMapper.getDeductRule();
    BigDecimal basicReduce = (BigDecimal) ruleMap.get("basicReduce");
    BigDecimal validReduce = (BigDecimal) ruleMap.get("validReduce");

    Map<String, BigDecimal> customerScoreMap = new HashMap<>();
    // 存储基础户加分、扣分，有效户加分、扣分，用于记录日志
    Map<String, Map<String, BigDecimal>> customerDetailMap = new HashMap<>();
    for (Map<String, Object> map : customerStatList) {
      String managerNo = (String) map.get("managerNo");

      long base0 = ((Number) map.get("base_tag0")).longValue();
      long base1 = ((Number) map.get("base_tag1")).longValue();
      long valid0 = ((Number) map.get("valid_tag0")).longValue();
      long valid1 = ((Number) map.get("valid_tag1")).longValue();

      BigDecimal base0Dec = new BigDecimal(base0);
      BigDecimal base1Dec = new BigDecimal(base1);
      BigDecimal valid0Dec = new BigDecimal(valid0);
      BigDecimal valid1Dec = new BigDecimal(valid1);

      // 基础户
      BigDecimal baseAdd = base1Dec.multiply(basePoint);
      BigDecimal baseDown = base0Dec.subtract(base1Dec).max(BigDecimal.ZERO);
      BigDecimal baseDeduct = baseDown.multiply(basicReduce);
      BigDecimal baseTotal = baseAdd.subtract(baseDeduct);

      // 有效户
      BigDecimal validAdd = valid1Dec.multiply(validPoint);
      BigDecimal validDown = valid0Dec.subtract(valid1Dec).max(BigDecimal.ZERO);
      BigDecimal validDeduct = validDown.multiply(validReduce);
      BigDecimal validTotal = validAdd.subtract(validDeduct);

      BigDecimal customerScore = baseTotal.add(validTotal).setScale(2, RoundingMode.HALF_UP);
      customerScoreMap.put(managerNo, customerScore);

      // 存储明细，用于插入积分记录
      Map<String, BigDecimal> detail = new HashMap<>();
      detail.put("baseAdd", baseAdd);
      detail.put("baseDeduct", baseDeduct);
      detail.put("validAdd", validAdd);
      detail.put("validDeduct", validDeduct);
      customerDetailMap.put(managerNo, detail);
    }

    // 2. 转换为Map结构
    Map<String, BigDecimal> baseScoreMap = convertListToMap(baseScoreList);
    Map<String, BigDecimal> productScoreMap = convertListToMap(productScoreList);
    Map<String, BigDecimal> shareScoreMap = convertListToMap(shareScoreList);

    // 3. 汇总所有客户经理ID
    Set<String> managerIdSet = new HashSet<>();
    managerIdSet.addAll(baseScoreMap.keySet());
    managerIdSet.addAll(productScoreMap.keySet());
    managerIdSet.addAll(shareScoreMap.keySet());
    managerIdSet.addAll(customerScoreMap.keySet());

    // 4. 遍历计算总积分 + 插入积分变动记录
    Map<String, BigDecimal> totalScoreMap = new HashMap<>();
    for (String managerId : managerIdSet) {
      // 获取各类积分
      BigDecimal baseScore = baseScoreMap.getOrDefault(managerId, ZERO);
      BigDecimal productScore = productScoreMap.getOrDefault(managerId, ZERO);
      BigDecimal shareScore = shareScoreMap.getOrDefault(managerId, ZERO);
      BigDecimal customerScore = customerScoreMap.getOrDefault(managerId, ZERO);

      // 计算总积分
      BigDecimal totalScore = baseScore.add(productScore)
          .add(shareScore)
          .add(customerScore)
          .setScale(2, RoundingMode.HALF_UP);
      totalScoreMap.put(managerId, totalScore);

      // ===================== 🔥 核心：查询客户经理姓名、支行 =====================
      Map<String, String> managerInfo = managerScoreChangeMapper.getManagerInfo(managerId);
      if (managerInfo == null) {
        throw new RuntimeException("客户经理不存在！");
      }
      String managerName = managerInfo.get("manager_name");
      String branchName = managerInfo.get("branch_name");

      // ===================== 🔥 1. 客群分类积分 插入记录 =====================
      if (baseScore.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "客群分类积分", "客户所属客群奖励", baseScore);
      }

      // ===================== 🔥 2. 产品积分 插入记录 =====================
      if (productScore.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "产品营销积分", "产品业务办理奖励", productScore);
      }

      // ===================== 🔥 3. 经验分享积分 插入记录 =====================
      if (shareScore.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "经验分享积分", "经验分享审核通过奖励", shareScore);
      }

      // ===================== 🔥 4. 基础户/有效户 积分/扣分 插入记录 =====================
      Map<String, BigDecimal> detail = customerDetailMap.getOrDefault(managerId, new HashMap<>());
      BigDecimal baseAdd = detail.getOrDefault("baseAdd", ZERO);
      BigDecimal baseDeduct = detail.getOrDefault("baseDeduct", ZERO);
      BigDecimal validAdd = detail.getOrDefault("validAdd", ZERO);
      BigDecimal validDeduct = detail.getOrDefault("validDeduct", ZERO);

      // 基础户加分
      if (baseAdd.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "基础户积分", "基础户数量达标奖励", baseAdd);
      }
      // 基础户下跌扣分（负数）
      if (baseDeduct.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "基础户下跌扣分", "基础户数量减少扣减", baseDeduct.negate());
      }
      // 有效户加分
      if (validAdd.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "有效户积分", "有效户数量达标奖励", validAdd);
      }
      // 有效户下跌扣分（负数）
      if (validDeduct.compareTo(ZERO) > 0) {
        saveScoreRecord(managerId, managerName, branchName,
            "有效户下跌扣分", "有效户数量减少扣减", validDeduct.negate());
      }

      // 更新段位和总积分
      String levelCode = managerScoreMapper.selectLevelCodeByScore(totalScore);
      Map<String, Object> param = new HashMap<>();
      param.put("managerId", managerId);
      param.put("totalScore", totalScore);
      param.put("levelCode", levelCode);
      managerScoreMapper.updateManagerScore(param);
    }

    // 5. 更新排名
    calculateAndUpdateRank(totalScoreMap);
    log.info("===== 客户经理积分计算完成 =====");
  }

  // ===================== 工具方法 =====================
  private Map<String, BigDecimal> convertListToMap(List<Map<String, Object>> list) {
    if (list == null || list.isEmpty()) {
      return new HashMap<>();
    }
    return list.stream()
        .filter(map -> map.get("managerId") != null)
        .collect(Collectors.toMap(
            map -> (String) map.get("managerId"),
            map -> {
              if (map.containsKey("baseScore")) return new BigDecimal(String.valueOf(map.get("baseScore")));
              if (map.containsKey("productScore")) return new BigDecimal(String.valueOf(map.get("productScore")));
              if (map.containsKey("shareScore")) return new BigDecimal(String.valueOf(map.get("shareScore")));
              return ZERO;
            }
        ));
  }

  private void calculateAndUpdateRank(Map<String, BigDecimal> totalScoreMap) {
    if (totalScoreMap.isEmpty()) return;

    // 按积分倒序排名
    List<Map.Entry<String, BigDecimal>> sortedList = totalScoreMap.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toList());

    for (int i = 0; i < sortedList.size(); i++) {
      Map<String, Object> param = new HashMap<>();
      param.put("managerId", sortedList.get(i).getKey());
      param.put("rankNum", i + 1);
      managerScoreMapper.updateManagerRank(param);
    }
  }

  @Override
  public List<Map<String, Object>> getManagerRankList() {
    return managerScoreMapper.selectManagerRankList();
  }

  @Override
  public Map<String, Object> getManagerScoreDetail(String managerId) {
    // 常量
    final String ACCOUNT_STATUS_OPEN = "1";
    final Integer SHARE_STATUS_PASS = 1;
    final BigDecimal ZERO = new BigDecimal("0.00");

    // 1. 查询三项积分明细
    BigDecimal baseScore = managerScoreMapper.selectSingleBaseScore(managerId, ACCOUNT_STATUS_OPEN);
    BigDecimal productScore = managerScoreMapper.selectSingleProductScore(managerId, ACCOUNT_STATUS_OPEN);
    BigDecimal shareScore = managerScoreMapper.selectSingleShareScore(managerId, SHARE_STATUS_PASS);

    // 空值处理
    baseScore = baseScore == null ? ZERO : baseScore;
    productScore = productScore == null ? ZERO : productScore;
    shareScore = shareScore == null ? ZERO : shareScore;

    // 2. 查询客户经理基础信息
    Map<String, Object> managerInfo = managerScoreMapper.selectManagerDetail(managerId);
    if (managerInfo == null) {
      throw new RuntimeException("客户经理不存在！");
    }

    // 3. 组装积分明细
    Map<String, Object> result = new HashMap<>();
    // 基础信息
    result.put("managerId", managerInfo.get("managerId"));
    result.put("managerName", managerInfo.get("managerName"));
    result.put("branchName", managerInfo.get("branchName"));
    result.put("levelName", managerInfo.get("levelName"));
    result.put("totalScore", managerInfo.get("totalScore"));
    // 积分明细（核心）
    result.put("baseScore", "客群分类基础积分：" + baseScore + " 分（多客群取最高）");
    result.put("productScore", "产品奖励积分：" + productScore + " 分（单个+3分，封顶6分）");
    result.put("shareScore", "经验分享积分：" + shareScore + " 分（审批通过1条+30分）");

    return result;
  }

  @Override
  public List<Map<String, Object>> getBranchRankList() {
    return managerScoreMapper.selectBranchRankList();
  }

  @Override
  public List<Map<String, Object>> getLevelRankList() {
    return managerScoreMapper.selectLevelRankList();
  }

  @Override
  public List<Map<String, Object>> getLevelCount() {
    return managerScoreMapper.selectLevelCount();
  }


  // ===================== 调用示例：积分变动时插入 =====================
  private void saveScoreRecord(String managerId, String managerName, String branchName,
      String treasureName, String treasureDesc, BigDecimal score) {
    ManagerScoreChange change = new ManagerScoreChange();
    change.setTreasureName(treasureName);
    change.setTreasureDesc(treasureDesc);
    change.setManagerId(managerId);
    change.setManagerName(managerName);
    change.setPost("客户经理");
    change.setBranchName(branchName);
    change.setScore(score);
    change.setTriggerTime(LocalDateTime.now());
    change.setStatus("通过");
    change.setRemark("系统自动计算");

    // 插入记录表
    managerScoreChangeMapper.insertScoreChange(change);
  }
}
