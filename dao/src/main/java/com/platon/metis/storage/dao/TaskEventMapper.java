package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskEvent;

import java.util.List;

public interface TaskEventMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskEvent record);

    int insertSelective(TaskEvent record);

    TaskEvent selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaskEvent record);

    int updateByPrimaryKey(TaskEvent record);

    List<TaskEvent> listTaskEventByTaskId(String taskId);

    void insertBatch(List<TaskEvent> taskEventList);
}