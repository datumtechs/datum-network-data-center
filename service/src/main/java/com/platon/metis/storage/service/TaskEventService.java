package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.TaskEvent;

import java.util.List;

public interface TaskEventService {
    List<TaskEvent> listTaskEventByTaskId(String taskId);

    void insert(List<TaskEvent> taskEventList);
}

