package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.TaskAlgoProvider;

import java.util.List;

public interface TaskAlgoProviderService {
    TaskAlgoProvider findAlgoProviderByTaskId(String taskId);

    void insert(TaskAlgoProvider taskAlgoProvider);

    void insertBatch(List<TaskAlgoProvider> taskAlgoProviderList);
}
