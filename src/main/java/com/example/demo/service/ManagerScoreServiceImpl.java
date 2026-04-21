package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.entity.ManagerScoreChange;
import com.example.demo.mapper.ManagerScoreChangeMapper;
import com.example.demo.mapper.ManagerScoreMapper;
import jakarta.annotation.Resource;
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

/**
 * 客户经理积分服务实现类
 * 负责计算和管理客户经理的积分、段位和排名
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerScoreServiceImpl implements ManagerScoreService {

  @Autowired
  private ManagerScoreMapper managerScoreMapper;

  // 注入Mapper
  @Resource
  private ManagerScoreChangeMapper managerScoreChangeMapper;


  // ===================== 固定常量 =====================
  private static final String ACCOUNT_STATUS_OPEN = "1";
  private static final Integer SHARE_STATUS_PASS = 1;
  private static final BigDecimal ZERO = new BigDecimal("0.00");

  // ===================== 核心入口方法 =====================
  /**
   * 执行客户经理积分全量计算
   * 包含：
   * 1. 更新已开户客户状态
   * 2. 加载加分规则
   * 3. 查询客户产品信息
   * 4. 查询客户经理排名信息
   * 5. 计算各类积分（客群、基础户、有效户、产品、经验分享）
   * 6. 应用积分倍数
   * 7. 更新段位和排名
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void calculateAllManagerScore() {
    log.info("===== 开始执行客户经理积分全量计算（保留2位小数） ======");

    // 1. 更新已开户客户的account_status为1
    managerScoreMapper.updateAccountStatusToOpened();
    log.info("已更新所有客户的开户状态为已开户");

    // 2. 查询加分规则
    List<Map<String, Object>> scoreAddRules = managerScoreMapper.selectScoreAddRules();
    Map<String, BigDecimal> addRuleMap = new HashMap<>();
    for (Map<String, Object> rule : scoreAddRules) {
      String itemName = (String) rule.get("itemName");
      BigDecimal points = new BigDecimal(String.valueOf(rule.get("points")));
      addRuleMap.put(itemName, points);
    }
    log.info("加载加分规则: {}", addRuleMap);

    // 3. 查询客户产品信息
    List<Map<String, Object>> customerProducts = managerScoreMapper.selectCustomerProducts();
    log.info("查询到 {} 个客户产品记录", customerProducts.size());

    // 4. 查询客户经理基础户增量排名
    List<Map<String, Object>> baseIncrementRank = managerScoreMapper.selectBaseCustomerIncrementRank();
    Map<String, Integer> baseRankMap = new HashMap<>();
    for (Map<String, Object> rank : baseIncrementRank) {
      baseRankMap.put((String) rank.get("managerId"), ((Number) rank.get("rank")).intValue());
    }
    log.info("基础户增量排名: {}", baseRankMap);

    // 5. 查询客户经理有效户增量排名
    List<Map<String, Object>> validIncrementRank = managerScoreMapper.selectValidCustomerIncrementRank();
    Map<String, Integer> validRankMap = new HashMap<>();
    for (Map<String, Object> rank : validIncrementRank) {
      validRankMap.put((String) rank.get("managerId"), ((Number) rank.get("rank")).intValue());
    }
    log.info("有效户增量排名: {}", validRankMap);

    // 6. 查询客户号对应的客户经理ID、客群编码和客户名称
    List<Map<String, Object>> customerInfoList = managerScoreMapper.selectCustomerInfoWithGroupCode();
    log.info("查询到 {} 个已开户客户", customerInfoList.size());

    // 7. 查询客户号对应的基础户/有效户状态
    List<Map<String, Object>> customerDetailList = managerScoreMapper.selectCustomerDetailStatus();
    log.info("查询到 {} 个客户的基础户/有效户状态", customerDetailList.size());

    // 8. 构建客户基础户/有效户状态映射（只考虑最新类 tag_type=1）
    Map<String, Map<String, Boolean>> customerStatusMap = new HashMap<>();
    for (Map<String, Object> map : customerDetailList) {
      Integer tagType = (Integer) map.get("tagType");
      if (tagType != 1) {
        continue; // 只处理最新类
      }
      String customerId = String.valueOf(map.get("customerId"));
      Map<String, Boolean> statusMap = new HashMap<>();
      statusMap.put("isBase", map.get("isBase") != null && (Integer) map.get("isBase") == 1);
      statusMap.put("isValid", map.get("isValid") != null && (Integer) map.get("isValid") == 1);
      customerStatusMap.put(customerId, statusMap);
    }

    // 9. 构建客户产品映射
    Map<String, Map<String, List<String>>> customerProductMap = new HashMap<>();
    for (Map<String, Object> product : customerProducts) {
      String customerNo = (String) product.get("customerNo");
      String managerId = (String) product.get("managerId");
      String productName = (String) product.get("productName");
      
      if (!customerProductMap.containsKey(managerId)) {
        customerProductMap.put(managerId, new HashMap<>());
      }
      
      Map<String, List<String>> managerProducts = customerProductMap.get(managerId);
      if (!managerProducts.containsKey(customerNo)) {
        managerProducts.put(customerNo, new ArrayList<>());
      }
      
      managerProducts.get(customerNo).add(productName);
    }

    // 10. 计算积分并插入变动记录
    Map<String, BigDecimal> totalScoreMap = new HashMap<>();

    for (Map<String, Object> customerInfo : customerInfoList) {
      String customerNo = String.valueOf(customerInfo.get("customerId"));
      String managerId = (String) customerInfo.get("managerId");
      String groupCode = (String) customerInfo.get("groupCode");
      String customerName = (String) customerInfo.get("customerName");

      if (managerId == null || managerId.isEmpty()) {
        continue;
      }

      // 查询客户经理信息
      Map<String, String> managerInfo = managerScoreChangeMapper.getManagerInfo(managerId);
      if (managerInfo == null) {
        throw new RuntimeException("客户经理不存在！");
      }
      String managerName = managerInfo.get("manager_name");
      String branchName = managerInfo.get("branch_name");

      // 初始化客户经理总积分
      if (!totalScoreMap.containsKey(managerId)) {
        totalScoreMap.put(managerId, ZERO);
      }

      // 5.1 根据group_code给manager_id加分
      if (groupCode != null && !groupCode.isEmpty()) {
        // 这里简化处理，实际应该根据group_code查询对应的积分值
        // 假设每个group_code对应10分
        BigDecimal groupScore = new BigDecimal("10.00");
        totalScoreMap.put(managerId, totalScoreMap.get(managerId).add(groupScore));
        // 客群分类积分变更不需要添加到积分变更记录
      }

      // 5.2 检查客户是否为基础户/有效户并加分
      Map<String, Boolean> statusMap = customerStatusMap.get(customerNo);
      if (statusMap != null) {
        if (statusMap.get("isBase")) {
          BigDecimal baseScore = addRuleMap.getOrDefault("base", new BigDecimal("0.00")); // 基础户加分
          if (baseScore.compareTo(ZERO) > 0) {
            totalScoreMap.put(managerId, totalScoreMap.get(managerId).add(baseScore));
            // 插入基础户积分记录
            saveScoreRecord(managerId, managerName, branchName,
                "基础户积分", customerName + "基础户达标", baseScore, customerNo, customerName, null, null);
          }
        }
        if (statusMap.get("isValid")) {
          BigDecimal validScore = addRuleMap.getOrDefault("valid", new BigDecimal("0.00")); // 有效户加分
          if (validScore.compareTo(ZERO) > 0) {
            totalScoreMap.put(managerId, totalScoreMap.get(managerId).add(validScore));
            // 插入有效户积分记录
            saveScoreRecord(managerId, managerName, branchName,
                "有效户积分", customerName + "有效户达标", validScore, customerNo, customerName, null, null);
          }
        }
      }

      // 5.3 检查客户是否有产品并加分（一个产品3分，封顶6分）
      Map<String, List<String>> managerProducts = customerProductMap.get(managerId);
      if (managerProducts != null) {
        List<String> products = managerProducts.get(customerNo);
        if (products != null && !products.isEmpty()) {
          // 计算产品得分，最多2个产品（6分）
          int productCount = Math.min(products.size(), 2);
          BigDecimal productScore = new BigDecimal(productCount * 3);
          totalScoreMap.put(managerId, totalScoreMap.get(managerId).add(productScore));
          
          // 构建产品名称字符串
          String productNames = String.join("、", products);
          // 插入产品积分记录
          saveScoreRecord(managerId, managerName, branchName,
              "产品积分", customerName + "有" + productNames, productScore, customerNo, customerName, null, null);
        }
      }

      // 5.4 计算积分倍数
      // 1. 计算客户标签数量
      String tags = (String) customerInfo.get("tags");
      int tagCount = 0;
      if (tags != null && !tags.isEmpty()) {
        tagCount = tags.split(",").length;
      }
      
      // 2. 确定倍数
      BigDecimal multiplier = new BigDecimal("1.0");
      String multiplierReason = "";
      
      // 标签数量倍数：3个及以上标签 1.5倍
      if (tagCount >= 3) {
        multiplier = new BigDecimal("1.5");
        multiplierReason = "客户标签数量达到3个及以上";
      }
      // 有效户增量排名前20：1.4倍
      else if (validRankMap.containsKey(managerId) && validRankMap.get(managerId) <= 20) {
        multiplier = new BigDecimal("1.4");
        multiplierReason = "有效户增量排名前20";
      }
      // 基础户增量排名前10：1.3倍
      else if (baseRankMap.containsKey(managerId) && baseRankMap.get(managerId) <= 10) {
        multiplier = new BigDecimal("1.3");
        multiplierReason = "基础户增量排名前10";
      }
      
      // 3. 应用倍数（如果倍数大于1）
      if (multiplier.compareTo(new BigDecimal("1.0")) > 0) {
        // 计算当前客户经理的积分
        BigDecimal currentScore = totalScoreMap.get(managerId);
        // 计算增量
        BigDecimal scoreIncrease = currentScore.multiply(multiplier.subtract(new BigDecimal("1.0")));
        // 更新总积分
        totalScoreMap.put(managerId, currentScore.add(scoreIncrease));
        
        // 插入倍数积分记录
        saveScoreRecord(managerId, managerName, branchName,
            "倍数积分", multiplierReason + "，积分放大" + multiplier + "倍", scoreIncrease, customerNo, customerName, null, null);
      }
    }

    // 12. 计算基础户和有效户减少的扣分
    List<Map<String, Object>> customerStatList = managerScoreMapper.statCustomerCount();
    Map<String, Object> ruleMap = managerScoreMapper.getDeductRule();
    BigDecimal basicReduce = (BigDecimal) ruleMap.get("basicReduce");
    BigDecimal validReduce = (BigDecimal) ruleMap.get("validReduce");

    for (Map<String, Object> map : customerStatList) {
      String managerId = (String) map.get("managerNo");

      long base0 = ((Number) map.get("base_tag0")).longValue(); // 基数类基础户
      long base1 = ((Number) map.get("base_tag1")).longValue(); // 最新类基础户
      long valid0 = ((Number) map.get("valid_tag0")).longValue(); // 基数类有效户
      long valid1 = ((Number) map.get("valid_tag1")).longValue(); // 最新类有效户

      // 计算减少量
      long baseDecrease = Math.max(0, base0 - base1);
      long validDecrease = Math.max(0, valid0 - valid1);

      if (baseDecrease > 0 || validDecrease > 0) {
        // 查询客户经理信息
        Map<String, String> managerInfo = managerScoreChangeMapper.getManagerInfo(managerId);
        if (managerInfo == null) {
          throw new RuntimeException("客户经理不存在！");
        }
        String managerName = managerInfo.get("manager_name");
        String branchName = managerInfo.get("branch_name");

        // 计算扣分数
        if (baseDecrease > 0) {
          BigDecimal baseDeduct = new BigDecimal(baseDecrease).multiply(basicReduce);
          totalScoreMap.put(managerId, totalScoreMap.getOrDefault(managerId, ZERO).subtract(baseDeduct));
          // 插入基础户下跌扣分记录
          saveScoreRecord(managerId, managerName, branchName,
              "基础户下跌扣分", "基础户数量减少扣减", baseDeduct.negate(), "", "", null, null);
        }

        if (validDecrease > 0) {
          BigDecimal validDeduct = new BigDecimal(validDecrease).multiply(validReduce);
          totalScoreMap.put(managerId, totalScoreMap.getOrDefault(managerId, ZERO).subtract(validDeduct));
          // 插入有效户下跌扣分记录
          saveScoreRecord(managerId, managerName, branchName,
              "有效户下跌扣分", "有效户数量减少扣减", validDeduct.negate(), "", "", null, null);
        }
      }
    }

    // 13. 计算经验分享积分
    List<Map<String, Object>> experienceShares = managerScoreMapper.selectExperienceShares();
    log.info("查询到 {} 个已审批通过的经验分享", experienceShares.size());

    for (Map<String, Object> share : experienceShares) {
      Long shareId = (Long) share.get("shareId");
      String managerId = (String) share.get("managerId");
      String shareTitle = (String) share.get("shareTitle");

      if (managerId == null || managerId.isEmpty()) {
        continue;
      }

      // 查询客户经理信息
      Map<String, String> managerInfo = managerScoreChangeMapper.getManagerInfo(managerId);
      if (managerInfo == null) {
        throw new RuntimeException("客户经理不存在！");
      }
      String managerName = managerInfo.get("manager_name");
      String branchName = managerInfo.get("branch_name");

      // 每个经验分享加30分
      BigDecimal shareScore = new BigDecimal("30.00");
      totalScoreMap.put(managerId, totalScoreMap.getOrDefault(managerId, ZERO).add(shareScore));

      // 插入经验分享积分记录
      saveScoreRecord(managerId, managerName, branchName,
          "经验分享积分", shareTitle + "经验分享审核通过", shareScore, "", "", shareId, shareTitle);
    }

    // 14. 更新段位和总积分
    for (Map.Entry<String, BigDecimal> entry : totalScoreMap.entrySet()) {
      String managerId = entry.getKey();
      BigDecimal totalScore = entry.getValue().setScale(2, RoundingMode.HALF_UP);

      // 更新段位和总积分
      String levelCode = managerScoreMapper.selectLevelCodeByScore(totalScore);
      Map<String, Object> param = new HashMap<>();
      param.put("managerId", managerId);
      param.put("totalScore", totalScore);
      param.put("levelCode", levelCode);
      managerScoreMapper.updateManagerScore(param);
    }

    // 15. 更新排名
    calculateAndUpdateRank(totalScoreMap);
    log.info("===== 客户经理积分计算完成 =====");
  }

  /**
   * 计算并更新客户经理排名
   * @param totalScoreMap 客户经理总积分映射
   */
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

  /**
   * 查询支行排名列表
   * @return 支行排名列表
   */
  @Override
  public List<Map<String, Object>> getBranchRankList() {
    return managerScoreMapper.selectBranchRankList();
  }

  /**
   * 查询段位排名列表
   * @return 段位排名列表
   */
  @Override
  public List<Map<String, Object>> getLevelRankList() {
    return managerScoreMapper.selectLevelRankList();
  }

  /**
   * 查询段位数量
   * @return 段位数量列表
   */
  @Override
  public List<Map<String, Object>> getLevelCount() {
    return managerScoreMapper.selectLevelCount();
  }

  /**
   * 获取所有客户经理的积分变更记录（分页）
   * @param page 页码
   * @param size 每页大小
   * @return 积分变更记录列表
   */
  @Override
  public PageResponse<Map<String, Object>> getScoreChangeList(int page, int size) {
    int offset = (page - 1) * size;
    List<Map<String, Object>> list = managerScoreMapper.selectScoreChangeList(offset, size);
    long total = managerScoreMapper.countScoreChangeList();
    return PageResponse.success(total, page, size, list);
  }

  /**
   * 获取单个客户经理的积分变更记录（分页）
   * @param managerId 客户经理ID
   * @param page 页码
   * @param size 每页大小
   * @return 积分变更记录列表
   */
  @Override
  public PageResponse<Map<String, Object>> getScoreChangeListByManagerId(String managerId, int page, int size) {
    int offset = (page - 1) * size;
    List<Map<String, Object>> list = managerScoreMapper.selectScoreChangeListByManagerId(managerId, offset, size);
    long total = managerScoreMapper.countScoreChangeListByManagerId(managerId);
    return PageResponse.success(total, page, size, list);
  }


  /**
   * 保存积分变动记录
   * @param managerId 客户经理ID
   * @param managerName 客户经理名称
   * @param branchName 所属支行
   * @param treasureName 积分类型名称
   * @param treasureDesc 积分变动描述
   * @param score 积分值
   * @param customerNo 客户号
   * @param customerName 客户名称
   * @param shareId 经验分享ID
   * @param shareTitle 经验分享标题
   */
  private void saveScoreRecord(String managerId, String managerName, String branchName,
      String treasureName, String treasureDesc, BigDecimal score, String customerNo, String customerName, Long shareId, String shareTitle) {
    // 检查是否存在重复记录
    // 重复条件：相同的客户经理ID、积分类型、客户号（或为空）、分享ID（或为空）
    int count = managerScoreMapper.checkDuplicateScoreChange(managerId, treasureName, customerNo, shareId);
    if (count > 0) {
      log.info("积分变动记录已存在，跳过插入: managerId={}, treasureName={}, customerNo={}, shareId={}", 
              managerId, treasureName, customerNo, shareId);
      return;
    }

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
    change.setCustomerNo(customerNo);
    change.setCustomerName(customerName);
    change.setShareId(shareId);
    change.setShareTitle(shareTitle);

    // 插入记录表
    managerScoreChangeMapper.insertScoreChange(change);
    log.info("积分变动记录插入成功: managerId={}, treasureName={}, score={}", managerId, treasureName, score);

    // 立即更新客户经理的积分、段位和排名
    updateManagerScoreAndRank(managerId);
  }

  /**
   * 积分变动后更新客户经理的总积分、段位和排名
   * @param managerId 客户经理ID
   */
  private void updateManagerScoreAndRank(String managerId) {
    // 计算客户经理的最新总积分
    BigDecimal totalScore = managerScoreMapper.calculateTotalScore(managerId);
    
    // 查询对应的段位
    String levelCode = managerScoreMapper.selectLevelCodeByScore(totalScore);
    
    // 更新积分和段位
    Map<String, Object> param = new HashMap<>();
    param.put("managerId", managerId);
    param.put("totalScore", totalScore.setScale(2, RoundingMode.HALF_UP));
    param.put("levelCode", levelCode);
    managerScoreMapper.updateManagerScore(param);
    
    // 更新排名
    managerScoreMapper.updateRanking();
  }
}
