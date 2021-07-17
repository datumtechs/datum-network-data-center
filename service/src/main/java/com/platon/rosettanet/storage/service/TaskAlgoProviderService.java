package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskAlgoProvider;

import java.util.List;

public interface TaskAlgoProviderService {
    TaskAlgoProvider findAlgoProviderByTaskId(String taskId);

    void insert(TaskAlgoProvider taskAlgoProvider);

    void insertBatch(List<TaskAlgoProvider> taskAlgoProviderList);
}
