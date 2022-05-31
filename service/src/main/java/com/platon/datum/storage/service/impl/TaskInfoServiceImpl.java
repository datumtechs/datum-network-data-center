package com.platon.datum.storage.service.impl;

import com.google.protobuf.ProtocolStringList;
import com.platon.datum.storage.dao.TaskInfoMapper;
import com.platon.datum.storage.dao.entity.TaskInfo;
import com.platon.datum.storage.service.TaskInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:14
 * @Version
 * @Desc
 */

@Service
@Slf4j
public class TaskInfoServiceImpl implements TaskInfoService {

    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Override
    public void saveTask(TaskInfo taskInfo) {
        taskInfoMapper.insertSelective(taskInfo);
    }

    @Override
    public TaskInfo findByTaskId(String taskId) {
        return taskInfoMapper.selectByPrimaryKey(taskId);
    }

    @Override
    public List<TaskInfo> syncTaskInfo(LocalDateTime lastUpdatedAt, long limit) {
        return taskInfoMapper.syncTaskInfo(lastUpdatedAt, limit);
    }

    @Override
    public List<TaskInfo> listTaskInfoByIdentityId(String identityId, LocalDateTime lastUpdateAt, long pageSize) {
        return taskInfoMapper.listTaskInfoByIdentityId(identityId, lastUpdateAt, pageSize);
    }

    @Override
    public List<TaskInfo> listTaskInfoByTaskIds(ProtocolStringList taskIdsList) {
        return taskInfoMapper.listTaskInfoByTaskIds(taskIdsList);
    }
}
