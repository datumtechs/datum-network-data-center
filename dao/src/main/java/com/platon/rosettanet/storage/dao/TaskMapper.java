package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.Task;

import java.util.List;

public interface TaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(Task record);

    int insertSelective(Task record);

    Task selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Task record);

    int updateByPrimaryKey(Task record);

    List<Task> listTask();

    void insertBatch(List<Task> taskList);

    List<Task> listTaskByIdentityId(String identityId);
}