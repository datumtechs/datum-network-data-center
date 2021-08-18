package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskPowerProvider;

import java.util.List;

public interface TaskPowerProviderService {
    void insert(List<TaskPowerProvider> taskPowerProviderList);
    List<TaskPowerProvider> listTaskPowerProvider(String taskId);


    /**
     * 统计组织以算力提供方参与的任务数量
     * @param identityId
     * @return
     */
    int countTaskAsPowerProvider(String identityId);
}

