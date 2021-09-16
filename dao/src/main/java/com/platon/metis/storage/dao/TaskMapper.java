package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(Task record);

    Task selectByPrimaryKey(String id);

    int updateByPrimaryKey(Task record);

    List<Task> syncTask(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt);

    void insertBatch(List<Task> taskList);

    List<Task> listTaskByIdentityId(String identityId);
}