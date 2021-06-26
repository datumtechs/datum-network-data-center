package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.TaskMetaData;

import java.util.List;

public interface TaskMetaDataMapper {
    int insert(TaskMetaData taskMetaData);
    void insertBatch(List<TaskMetaData> taskMetaDataList);
    List<TaskMetaData> listTaskMetaData(String taskId);
}
