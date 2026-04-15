package com.example.demo.mapper;

import com.example.demo.entity.ExperienceShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExperienceShareMapper {
  int insert(ExperienceShare share);
  int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);
  ExperienceShare selectById(@Param("id") Long id);
  List<ExperienceShare> selectList(@Param("status") Integer status);

  // 新增：查询待审批数量
  Integer getPendingAuditCount();
}