package com.platon.datum.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.datum.storage.dao.TaskPowerPolicyOptionsPartMapper;
import com.platon.datum.storage.dao.entity.TaskPowerPolicyOptionsPart;
import com.platon.datum.storage.service.BaseService;
import com.platon.datum.storage.service.TaskPowerOptionPartService;
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
public class TaskPowerOptionPartServiceImpl extends BaseService implements TaskPowerOptionPartService {

    @Resource
    private TaskPowerPolicyOptionsPartMapper taskPowerPolicyOptionsPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void savePowerOption(String taskId, List<String> powerPolicyOptionList) {
        String powerPolicyOption = list2string(powerPolicyOptionList);
        String[] partArray = StrUtil.split(powerPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskPowerPolicyOptionsPart optionPart = new TaskPowerPolicyOptionsPart();
            optionPart.setTaskId(taskId);
            optionPart.setPowerPolicyOptionsPart(partArray[i]);
            taskPowerPolicyOptionsPartMapper.insertSelective(optionPart);
        }
    }


    @Override
    public List<String> getPowerOption(String taskId) {
        List<TaskPowerPolicyOptionsPart> list = taskPowerPolicyOptionsPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getPowerPolicyOptionsPart());
        });
        return string2list(sb.toString());
    }
}
