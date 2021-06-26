package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.Task;

import java.util.List;

public interface TaskService {
    int insert(Task task);
    Task findByPK(String taskId);
    List<Task> listTask();
    int countTask(String ownerIdentityId);
}
