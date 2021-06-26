package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.TaskMetaDataColumn;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskMetaDataColumnMapper {
    int deleteByPrimaryKey(@Param("taskId") String taskId, @Param("metaDataId") String metaDataId, @Param("columnIdx") Integer columnIdx);

    int insert(TaskMetaDataColumn record);

    int insertSelective(TaskMetaDataColumn record);

    void insertBatch(List<TaskMetaDataColumn> taskMetaDataColumnList);

    List<TaskMetaDataColumn> listTaskMetaDataColumn(@Param("taskId") String taskId, @Param("metaDataId") String metaDataId);
}