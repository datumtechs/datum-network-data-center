package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.TaskMetaDataColumnMapper;
import com.platon.rosettanet.storage.dao.entity.TaskMetaDataColumn;
import com.platon.rosettanet.storage.service.TaskMetaDataColumnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskMetaDataColumnServiceImpl implements TaskMetaDataColumnService {
    @Autowired
    private TaskMetaDataColumnMapper taskMetaDataColumnMapper;


    @Override
    public int insert(TaskMetaDataColumn taskMetaDataColumn) {
        return taskMetaDataColumnMapper.insert(taskMetaDataColumn);
    }

    @Override
    public void insert(List<TaskMetaDataColumn> taskMetaDataColumnList) {
        taskMetaDataColumnMapper.insertBatch(taskMetaDataColumnList);
    }

    @Override
    public List<TaskMetaDataColumn> listTaskMetaDataColumn(String taskId, String metaDataId){
        return taskMetaDataColumnMapper.listTaskMetaDataColumn(taskId, metaDataId);
    }
}
