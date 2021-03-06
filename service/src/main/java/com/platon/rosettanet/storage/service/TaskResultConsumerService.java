package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskResultConsumer;

import java.util.List;

public interface TaskResultConsumerService {
    void insert(List<TaskResultConsumer> taskResultConsumerList);

    /**
     * 查询结果按consumerIdentityId排序，方便后续处理
     * @param taskId
     * @return
     */
    List<TaskResultConsumer> listTaskResultConsumer(String taskId);
}
