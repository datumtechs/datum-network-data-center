package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.TaskMapper;
import com.platon.metis.storage.dao.entity.Task;
import com.platon.metis.storage.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public int insert(Task task) {
        return taskMapper.insert(task);
    }

    @Override
    public void insert(List<Task> taskList) {
        taskMapper.insertBatch(taskList);
    }

    @Override
    public Task findByPK(String taskId){
       return taskMapper.selectByPrimaryKey(taskId);
    }

    @Override
    public List<Task> syncTask(LocalDateTime lastUpdatedAt, long limit) {
        return taskMapper.syncTask(lastUpdatedAt, limit);
    }

    @Override
    public List<Task> listTaskByIdentityId(String identityId, LocalDateTime lastUpdatedAt, long limit) {
        return taskMapper.listTaskByIdentityId(identityId, lastUpdatedAt, limit);
    }
}
