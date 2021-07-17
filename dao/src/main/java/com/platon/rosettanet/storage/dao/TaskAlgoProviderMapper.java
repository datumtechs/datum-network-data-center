package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.TaskAlgoProvider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskAlgoProviderMapper {
    TaskAlgoProvider selectByPrimaryKey(@Param("taskId") String taskId);

    void insert(TaskAlgoProvider taskAlgoProvider);

    void insertBatch(List<TaskAlgoProvider> taskAlgoProviderList);
}
