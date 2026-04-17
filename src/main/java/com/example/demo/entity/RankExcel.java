package com.example.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 客户经理排行榜 Excel 导出实体
 */
@Data
public class RankExcel {

  @ExcelProperty(value = "排名", index = 0)
  private Integer rankNum;

  @ExcelProperty(value = "客户经理ID", index = 1)
  private String managerId;

  @ExcelProperty(value = "客户经理姓名", index = 2)
  private String managerName;

  @ExcelProperty(value = "所属支行", index = 3)
  private String branchName;

  @ExcelProperty(value = "总积分(2位小数)", index = 4)
  private BigDecimal totalScore;

  @ExcelProperty(value = "当前段位", index = 5)
  private String levelName;
}
