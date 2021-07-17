package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.TaskAlgoProviderMapper;
import com.platon.rosettanet.storage.dao.entity.TaskAlgoProvider;
import com.platon.rosettanet.storage.service.TaskAlgoProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskAlgoProviderServiceImpl implements TaskAlgoProviderService {

    @Autowired
    private TaskAlgoProviderMapper taskAlgoProviderMapper;


    @Override
    public TaskAlgoProvider findAlgoProviderByTaskId(String taskId) {
        return taskAlgoProviderMapper.selectByPrimaryKey(taskId);
    }

    @Override
    public void insert(TaskAlgoProvider taskAlgoProvider) {
        taskAlgoProviderMapper.insert(taskAlgoProvider);
    }

    @Override
    public void insertBatch(List<TaskAlgoProvider> taskAlgoProviderList) {
        taskAlgoProviderMapper.insertBatch(taskAlgoProviderList);
    }
}
