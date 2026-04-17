package com.example.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LevelExcel {

  @ExcelProperty(value = "姓名", index = 0)
  private String managerName;

  @ExcelProperty(value = "所属单位", index = 1)
  private String branchName;

  @ExcelProperty(value = "累计积分", index = 2)
  private BigDecimal totalScore;

  @ExcelProperty(value = "段位", index = 3)
  private String levelName;
}