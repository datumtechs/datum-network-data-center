package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskPowerProvider;

import java.util.List;

public interface TaskPowerProviderService {
    void insert(List<TaskPowerProvider> taskPowerProviderList);
    List<TaskPowerProvider> listTaskPowerProvider(String taskId);
}

