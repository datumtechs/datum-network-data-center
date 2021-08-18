package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.TaskPowerProviderMapper;
import com.platon.rosettanet.storage.dao.entity.TaskPowerProvider;
import com.platon.rosettanet.storage.service.TaskPowerProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskPowerProviderServiceImpl implements TaskPowerProviderService {

    @Autowired
    private TaskPowerProviderMapper taskPowerProviderMapper;



    @Override
    public void insert(List<TaskPowerProvider> taskPowerProviderList) {
         taskPowerProviderMapper.insertBatch(taskPowerProviderList);
    }

    @Override
    public List<TaskPowerProvider> listTaskPowerProvider(String taskId) {
        return taskPowerProviderMapper.listTaskPowerProvider(taskId);
    }

    @Override
    public int countTaskAsPowerProvider(String identityId) {
        return taskPowerProviderMapper.countTaskAsPowerProvider(identityId);
    }
}
