package com.example.demo.mapper;

import com.example.demo.entity.ManagerScoreChange;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ManagerScoreChangeMapper {

  /**
   * 插入客户经理积分变动记录
   */
  void insertScoreChange(ManagerScoreChange change);

  // ManagerScoreMapper.java
  Map<String, String> getManagerInfo(String managerId);
}
