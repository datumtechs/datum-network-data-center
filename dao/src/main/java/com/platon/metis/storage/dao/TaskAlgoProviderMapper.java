package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskAlgoProvider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskAlgoProviderMapper {
    TaskAlgoProvider selectByPrimaryKey(@Param("taskId") String taskId);

    void insert(TaskAlgoProvider taskAlgoProvider);

    void insertBatch(List<TaskAlgoProvider> taskAlgoProviderList);
}
