package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.TaskMetaDataMapper;
import com.platon.rosettanet.storage.dao.entity.TaskMetaData;
import com.platon.rosettanet.storage.service.TaskMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskMetaDataServiceImpl implements TaskMetaDataService {

    @Autowired
    private TaskMetaDataMapper taskMetaDataMapper;



    @Override
    public int insert(TaskMetaData taskMetaData) {
        return taskMetaDataMapper.insert(taskMetaData);
    }

    @Override
    public void insert(List<TaskMetaData> taskMetaDataList) {
        taskMetaDataMapper.insertBatch(taskMetaDataList);
    }

    @Override
    public List<TaskMetaData> listTaskMetaData(String taskId) {
        return taskMetaDataMapper.listTaskMetaData(taskId);
    }
}
