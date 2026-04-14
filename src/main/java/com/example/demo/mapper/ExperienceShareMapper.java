package com.example.demo.mapper;


import com.example.demo.entity.ExperienceShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExperienceShareMapper {

  // 1. 新增经验分享
  int insert(ExperienceShare share);

  // 2. 更新审批信息
  int updateAudit(ExperienceShare share);

  // 3. 根据ID查询详情
  ExperienceShare selectById(@Param("id") Long id);

  // 4. 分页查询列表（支持按状态筛选）
  List<ExperienceShare> selectList(@Param("status") Integer status);
}
