package com.example.demo.service;
import com.example.demo.mapper.ManagerScoreMapper;
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

    // 2. 转换为Map结构（BigDecimal 支持2位小数）
    Map<String, BigDecimal> baseScoreMap = convertListToMap(baseScoreList);
    Map<String, BigDecimal> productScoreMap = convertListToMap(productScoreList);
    Map<String, BigDecimal> shareScoreMap = convertListToMap(shareScoreList);

    // 3. 汇总所有客户经理ID
    Set<String> managerIdSet = new HashSet<>();
    managerIdSet.addAll(baseScoreMap.keySet());
    managerIdSet.addAll(productScoreMap.keySet());
    managerIdSet.addAll(shareScoreMap.keySet());

    // 4. 遍历计算总积分 + 段位
    Map<String, BigDecimal> totalScoreMap = new HashMap<>();
    for (String managerId : managerIdSet) {
      // 安全获取积分，默认0.00
      BigDecimal baseScore = baseScoreMap.getOrDefault(managerId, ZERO);
      BigDecimal productScore = productScoreMap.getOrDefault(managerId, ZERO);
      BigDecimal shareScore = shareScoreMap.getOrDefault(managerId, ZERO);

      // 总积分（保留2位小数）
      BigDecimal totalScore = baseScore.add(productScore)
          .add(shareScore)
          .setScale(2, RoundingMode.HALF_UP);

      totalScoreMap.put(managerId, totalScore);

      // 匹配段位
      String levelCode = managerScoreMapper.selectLevelCodeByScore(totalScore);

      // 更新数据库
      Map<String, Object> param = new HashMap<>();
      param.put("managerId", managerId);
      param.put("totalScore", totalScore);
      param.put("levelCode", levelCode);
      managerScoreMapper.updateManagerScore(param);
    }

    // 5. 更新排名
    calculateAndUpdateRank(totalScoreMap);

    log.info("===== 客户经理积分计算完成，共更新 {} 个客户经理 =====", managerIdSet.size());
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
}
