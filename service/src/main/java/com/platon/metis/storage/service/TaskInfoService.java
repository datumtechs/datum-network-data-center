package com.platon.metis.storage.service;

import com.google.protobuf.ProtocolStringList;
import com.platon.metis.storage.dao.entity.TaskInfo;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @Author liushuyu
 * @Date 2022/4/22 14:13
 * @Version
 * @Desc
 */
public interface TaskInfoService {
    void saveTask(TaskInfo taskInfo);

    TaskInfo findByTaskId(String taskId);

    List<TaskInfo> syncTaskInfo(LocalDateTime lastUpdatedAt, long limit);

    List<TaskInfo> listTaskInfoByIdentityId(String identityId, LocalDateTime lastUpdateAt, long pageSize);

    List<TaskInfo> listTaskInfoByTaskIds(ProtocolStringList taskIdsList);
}
