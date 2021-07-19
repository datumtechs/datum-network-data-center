package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskEvent;

import java.util.List;

public interface TaskEventService {
    List<TaskEvent> listTaskEventByTaskId(String taskId);

    void insert(List<TaskEvent> taskEventList);
}

