package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskMetaData;

import java.util.List;

public interface TaskMetaDataService {
    int insert(TaskMetaData taskMetaData);
    void insert(List<TaskMetaData> taskMetaDataList);
    List<TaskMetaData> listTaskMetaData(String taskId);
}
