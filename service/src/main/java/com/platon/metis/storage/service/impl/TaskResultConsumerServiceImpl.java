package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.TaskResultConsumerMapper;
import com.platon.metis.storage.dao.entity.TaskResultConsumer;
import com.platon.metis.storage.service.TaskResultConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskResultConsumerServiceImpl implements TaskResultConsumerService {

    @Autowired
    private TaskResultConsumerMapper taskResultConsumerMapper;

    @Override
    public void insert(List<TaskResultConsumer> taskResultConsumerList) {
        taskResultConsumerMapper.insertBatch(taskResultConsumerList);
    }

    @Override
    public List<TaskResultConsumer> listTaskResultConsumer(String taskId) {
        return taskResultConsumerMapper.listTaskResultConsumer(taskId);
    }
}
