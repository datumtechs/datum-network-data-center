package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.metis.storage.dao.TaskPowerOptionPartMapper;
import com.platon.metis.storage.dao.entity.TaskPowerOptionPart;
import com.platon.metis.storage.service.TaskPowerOptionPartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:46
 * @Version
 * @Desc
 */

@Slf4j
@Service
public class TaskPowerOptionPartServiceImpl implements TaskPowerOptionPartService {

    @Resource
    private TaskPowerOptionPartMapper taskPowerOptionPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void savePowerOption(String taskId, String powerPolicyOption) {
        String[] partArray = StrUtil.split(powerPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskPowerOptionPart optionPart = new TaskPowerOptionPart();
            optionPart.setTaskId(taskId);
            optionPart.setPowerOptionPart(partArray[i]);
            taskPowerOptionPartMapper.insertSelective(optionPart);
        }
    }


    @Override
    public String getPowerOption(String taskId) {
        List<TaskPowerOptionPart> list = taskPowerOptionPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getPowerOptionPart());
        });
        return sb.toString();
    }
}
