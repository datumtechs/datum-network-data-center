package com.platon.datum.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.datum.storage.dao.TaskDataPolicyOptionsPartMapper;
import com.platon.datum.storage.dao.entity.TaskDataPolicyOptionsPart;
import com.platon.datum.storage.service.BaseService;
import com.platon.datum.storage.service.TaskDataOptionPartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:27
 * @Version
 * @Desc
 */


@Slf4j
@Service
public class TaskDataOptionPartServiceImpl extends BaseService implements TaskDataOptionPartService {

    @Resource
    private TaskDataPolicyOptionsPartMapper taskDataPolicyOptionsPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void saveDataOption(String taskId, List<String> dataPolicyOptionList) {
        String dataPolicyOption = list2string(dataPolicyOptionList);
        String[] partArray = StrUtil.split(dataPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskDataPolicyOptionsPart optionPart = new TaskDataPolicyOptionsPart();
            optionPart.setTaskId(taskId);
            optionPart.setDataPolicyOptionsPart(partArray[i]);
            taskDataPolicyOptionsPartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public List<String> getDataOption(String taskId) {
        List<TaskDataPolicyOptionsPart> list = taskDataPolicyOptionsPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getDataPolicyOptionsPart());
        });
        return string2list(sb.toString());
    }

}
