package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.Task;

import java.util.List;

public interface TaskService {
    int insert(Task task);
    void insert(List<Task> taskList);

    Task findByPK(String taskId);
    List<Task> listTask();
    List<Task> listTaskByIdentityId(String identityId);

}
