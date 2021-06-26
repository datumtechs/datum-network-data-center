package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.TaskPowerProvider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskPowerProviderMapper {
    int deleteByPrimaryKey(@Param("taskId") String taskId, @Param("identityId") String identityId);

    int insert(TaskPowerProvider record);

    int insertSelective(TaskPowerProvider record);

    TaskPowerProvider selectByPrimaryKey(@Param("taskId") String taskId, @Param("identityId") String identityId);

    int updateByPrimaryKeySelective(TaskPowerProvider record);

    int updateByPrimaryKey(TaskPowerProvider record);

    void insertBatch(List<TaskPowerProvider> taskPowerProviderList);

    List<TaskPowerProvider> listTaskPowerProvider(String taskId);
}