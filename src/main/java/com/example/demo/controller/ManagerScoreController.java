package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.demo.entity.BranchExcel;
import com.example.demo.entity.LevelExcel;
import com.example.demo.entity.RankExcel;
import com.example.demo.service.ManagerScoreService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
   */
  @GetMapping("/refresh")
  public String refreshScore() {
    managerScoreService.calculateAllManagerScore();
    return "积分计算成功！";
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
   * 【积分明细接口】根据客户经理ID查询详细积分来源
   * @param managerId 客户经理8位ID
   * @return 积分明细JSON
   */
  @GetMapping("/detail")
  public ResponseEntity<Map<String, Object>> getScoreDetail(@RequestParam String managerId) {
    Map<String, Object> detail = managerScoreService.getManagerScoreDetail(managerId);
    return ResponseEntity.ok(detail);
  }

  // ===================== 【新增】导出排行榜为 Excel =====================
  @GetMapping("/export")
  public void exportRank(HttpServletResponse response) throws Exception {
    // 1. 获取排行榜数据
    List<Map<String, Object>> rankList = managerScoreService.getManagerRankList();

    // 2. 转换为 Excel 实体类
    List<RankExcel> excelList = rankList.stream().map(map -> {
      RankExcel excel = new RankExcel();
      excel.setRankNum(safeToInt( map.get("rankNum")));
      excel.setManagerId((String) map.get("managerId"));
      excel.setManagerName((String) map.get("managerName"));
      excel.setBranchName((String) map.get("branchName"));
      excel.setTotalScore((BigDecimal) map.get("totalScore"));
      excel.setLevelName((String) map.get("levelName"));
      return excel;
    }).collect(Collectors.toList());

    // 3. 设置响应头（浏览器下载）
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode("客户经理积分排行榜", "UTF-8").replaceAll("\\+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 4. EasyExcel 写出文件
    EasyExcel.write(response.getOutputStream(), RankExcel.class)
        .sheet("排行榜")
        .doWrite(excelList);
  }

  // ===================== 【单位排行榜接口】 =====================
  @GetMapping("/branch-rank")
  public ResponseEntity<List<Map<String, Object>>> getBranchRank() {
    return ResponseEntity.ok(managerScoreService.getBranchRankList());
  }

  // ===================== 【单位排行榜导出Excel】 =====================
  @GetMapping("/branch-export")
  public void exportBranchRank(HttpServletResponse response) throws Exception {
    List<Map<String, Object>> list = managerScoreService.getBranchRankList();

    // 设置下载头
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode("单位积分排行榜", "UTF-8").replace("+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 导出
    EasyExcel.write(response.getOutputStream(), BranchExcel.class)
        .sheet("单位排行榜")
        .doWrite(list.stream().map(m -> {
          BranchExcel e = new BranchExcel();
          e.setRankNum(safeToInt(m.get("rankNum")) );
          e.setBranchName((String) m.get("branchName"));

          // ✅ 修复这里：BigInteger 转 Integer
          Object userCountObj = m.get("userCount");
          int userCount = 0;
          if (userCountObj instanceof BigInteger) {
            userCount = ((BigInteger) userCountObj).intValue();
          } else if (userCountObj instanceof Long) {
            userCount = ((Long) userCountObj).intValue();
          }
          e.setUserCount(userCount);

          e.setTotalScore((BigDecimal) m.get("totalScore"));
          e.setAvgScore((BigDecimal) m.get("avgScore"));
          return e;
        }).collect(Collectors.toList()));
  }

  // ===================== 【段位排行榜（人员）】 =====================
  @GetMapping("/level-rank")
  public ResponseEntity<List<Map<String, Object>>> levelRank() {
    return ResponseEntity.ok(managerScoreService.getLevelRankList());
  }

  // ===================== 【各段位人数统计】 =====================
  @GetMapping("/level-count")
  public ResponseEntity<List<Map<String, Object>>> levelCount() {
    return ResponseEntity.ok(managerScoreService.getLevelCount());
  }

  // ===================== 【导出段位排行榜Excel】 =====================
  @GetMapping("/level-export")
  public void exportLevelRank(HttpServletResponse response) throws Exception {
    List<Map<String, Object>> list = managerScoreService.getLevelRankList();

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode("段位排行榜", "UTF-8").replace("+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    EasyExcel.write(response.getOutputStream(), LevelExcel.class)
        .sheet("段位排行榜")
        .doWrite(list.stream().map(m -> {
          LevelExcel excel = new LevelExcel();
          excel.setManagerName((String) m.get("managerName"));
          excel.setBranchName((String) m.get("branchName"));
          excel.setTotalScore((BigDecimal) m.get("totalScore"));
          excel.setLevelName((String) m.get("levelName"));
          return excel;
        }).collect(Collectors.toList()));
  }

  // ===================== 【三合一导出：个人+单位+段位】 =====================
  @GetMapping("/export-all")
  public void exportAll(HttpServletResponse response) throws Exception {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode("客户经理积分全量排行榜", "UTF-8").replace("+", "%20");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 获取3套数据
    List<Map<String, Object>> personal = managerScoreService.getManagerRankList();
    List<Map<String, Object>> branch = managerScoreService.getBranchRankList();
    List<Map<String, Object>> level = managerScoreService.getLevelRankList();

    // 转换成Excel实体
    List<RankExcel> list1 = personal.stream().map(p -> {
      RankExcel e = new RankExcel();
      e.setRankNum(safeToInt(p.get("rankNum")));
      e.setManagerName((String)p.get("managerName"));
      e.setBranchName((String)p.get("branchName"));
      e.setTotalScore((BigDecimal)p.get("totalScore"));
      e.setLevelName((String)p.get("levelName"));
      return e;
    }).collect(Collectors.toList());

    List<BranchExcel> list2 = branch.stream().map(p -> {
      BranchExcel e = new BranchExcel();
      e.setRankNum(safeToInt(p.get("rankNum")));
      e.setBranchName((String)p.get("branchName"));
      Object obj = p.get("userCount");
      int userCount = 0;
      if (obj instanceof BigInteger) {
        userCount = ((BigInteger) obj).intValue();
      } else if (obj instanceof Long) {
        userCount = ((Long) obj).intValue();
      }
      e.setUserCount(userCount);

      e.setTotalScore((BigDecimal)p.get("totalScore"));
      e.setAvgScore((BigDecimal)p.get("avgScore"));
      return e;
    }).collect(Collectors.toList());

    List<LevelExcel> list3 = level.stream().map(p -> {
      LevelExcel e = new LevelExcel();
      e.setManagerName((String)p.get("managerName"));
      e.setBranchName((String)p.get("branchName"));
      e.setTotalScore((BigDecimal)p.get("totalScore"));
      e.setLevelName((String)p.get("levelName"));
      return e;
    }).collect(Collectors.toList());

    // 开始写出Excel（3个Sheet）
    try (com.alibaba.excel.ExcelWriter writer = EasyExcel.write(response.getOutputStream()).build()) {

      // Sheet1：个人排名
      WriteSheet sheet1 = EasyExcel.writerSheet(0, "个人排名").head(RankExcel.class).build();
      writer.write(list1, sheet1);

      // Sheet2：单位排名
      WriteSheet sheet2 = EasyExcel.writerSheet(1, "单位排名").head(BranchExcel.class).build();
      writer.write(list2, sheet2);

      // Sheet3：段位排名
      WriteSheet sheet3 = EasyExcel.writerSheet(2, "段位排行榜").head(LevelExcel.class).build();
      writer.write(list3, sheet3);
    }
  }

  /**
   * 安全将数字转为 Integer，兼容 Long / BigInteger / int
   */
  private Integer safeToInt(Object obj) {
    if (obj == null) return 0;
    if (obj instanceof BigInteger) return ((BigInteger) obj).intValue();
    if (obj instanceof Long) return ((Long) obj).intValue();
    if (obj instanceof Integer) return (Integer) obj;
    return 0;
  }
}
