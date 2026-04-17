package com.example.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BranchExcel {

  @ExcelProperty(value = "排名", index = 0)
  private Integer rankNum;

  @ExcelProperty(value = "单位", index = 1)
  private String branchName;

  @ExcelProperty(value = "参赛人数", index = 2)
  private Integer userCount;

  @ExcelProperty(value = "总积分", index = 3)
  private BigDecimal totalScore;

  @ExcelProperty(value = "人均积分", index = 4)
  private BigDecimal avgScore;
}