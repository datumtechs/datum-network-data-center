package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.TaskPowerResourceOptionMapper;
import com.platon.metis.storage.dao.entity.TaskPowerResourceOption;
import com.platon.metis.storage.service.TaskPowerResourceOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:49
 * @Version
 * @Desc
 */


@Slf4j
@Service
public class TaskPowerResourceOptionServiceImpl implements TaskPowerResourceOptionService {

    @Resource
    private TaskPowerResourceOptionMapper taskPowerResourceOptionMapper;


    @Override
    public void savePowerResourceOption(List<TaskPowerResourceOption> powerResourceOptionsList) {
        taskPowerResourceOptionMapper.insertList(powerResourceOptionsList);
    }

    @Override
    public List<TaskPowerResourceOption> getPowerResourceOption(String taskId){
        return taskPowerResourceOptionMapper.selectByTaskId(taskId);
    }
}
