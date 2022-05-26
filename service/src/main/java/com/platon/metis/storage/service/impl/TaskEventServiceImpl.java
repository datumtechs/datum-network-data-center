package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.TaskEventMapper;
import com.platon.metis.storage.dao.entity.TaskEvent;
import com.platon.metis.storage.service.TaskEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class TaskEventServiceImpl implements TaskEventService {

    @Autowired
    private TaskEventMapper taskEventMapper;


    @Override
    public List<TaskEvent> listTaskEventByTaskId(String taskId) {
        return taskEventMapper.listTaskEventByTaskId(taskId);
    }

    @Override
    public void insert(List<TaskEvent> taskEventList) {
        if(taskEventList == null || taskEventList.isEmpty()){
            return;
        }
        taskEventMapper.insertBatch(taskEventList);
    }
}
