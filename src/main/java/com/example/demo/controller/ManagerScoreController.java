package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.example.demo.entity.RankExcel;
import com.example.demo.service.ManagerScoreService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
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
      excel.setRankNum((Integer) map.get("rankNum"));
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
}
