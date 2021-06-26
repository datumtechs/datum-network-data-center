package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskResultConsumer;

import java.util.List;

public interface TaskResultConsumerService {
    void insert(List<TaskResultConsumer> taskResultConsumerList);
    List<TaskResultConsumer> listTaskResultConsumer(String taskId);
}
