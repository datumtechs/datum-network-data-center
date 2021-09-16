package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskResultConsumer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskResultConsumerMapper {
    int deleteByPrimaryKey(@Param("taskId") String taskId, @Param("consumerIdentityId") String consumerIdentityId, @Param("producerIdentityId") String producerIdentityId);

    int insert(TaskResultConsumer record);

    int insertSelective(TaskResultConsumer record);

    void insertBatch(List<TaskResultConsumer> taskResultConsumerList);

    List<TaskResultConsumer> listTaskResultConsumer(String taskId);
}