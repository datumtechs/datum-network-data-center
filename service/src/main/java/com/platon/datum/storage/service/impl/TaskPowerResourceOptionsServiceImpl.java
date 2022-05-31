package com.platon.datum.storage.service.impl;

import com.platon.datum.storage.dao.TaskPowerResourceOptionsMapper;
import com.platon.datum.storage.dao.entity.TaskPowerResourceOptions;
import com.platon.datum.storage.service.TaskPowerResourceOptionsService;
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
public class TaskPowerResourceOptionsServiceImpl implements TaskPowerResourceOptionsService {

    @Resource
    private TaskPowerResourceOptionsMapper taskPowerResourceOptionsMapper;


    @Override
    public void savePowerResourceOption(List<TaskPowerResourceOptions> powerResourceOptionsList) {
        if(powerResourceOptionsList == null || powerResourceOptionsList.isEmpty()){
            return;
        }
        taskPowerResourceOptionsMapper.insertList(powerResourceOptionsList);
    }

    @Override
    public List<TaskPowerResourceOptions> getPowerResourceOption(String taskId){
        return taskPowerResourceOptionsMapper.selectByTaskId(taskId);
    }
}
