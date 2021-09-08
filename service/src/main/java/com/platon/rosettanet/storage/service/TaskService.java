package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    int insert(Task task);
    void insert(List<Task> taskList);

    Task findByPK(String taskId);
    List<Task> syncTask(LocalDateTime lastUpdatedAt);
    List<Task> listTaskByIdentityId(String identityId);

}
