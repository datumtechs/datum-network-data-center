package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.TaskEventMapper;
import com.platon.rosettanet.storage.dao.entity.TaskEvent;
import com.platon.rosettanet.storage.service.TaskEventService;
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
}
