package com.platon.datum.storage.service;

import com.platon.datum.storage.dao.entity.TaskEvent;

import java.util.List;

public interface TaskEventService {
    List<TaskEvent> listTaskEventByTaskId(String taskId);

    void insert(List<TaskEvent> taskEventList);
}

