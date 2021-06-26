package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.TaskMetaDataColumn;

import java.util.List;

public interface TaskMetaDataColumnService {
    int insert(TaskMetaDataColumn taskMetaDataColumn);
    void insert(List<TaskMetaDataColumn> taskMetaDataColumnList);
    List<TaskMetaDataColumn> listTaskMetaDataColumn(String taskId, String metaDataId);
}
