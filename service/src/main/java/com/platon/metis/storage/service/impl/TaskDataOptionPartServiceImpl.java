package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.metis.storage.dao.TaskDataFlowOptionPartMapper;
import com.platon.metis.storage.dao.TaskDataOptionPartMapper;
import com.platon.metis.storage.dao.entity.TaskDataFlowOptionPart;
import com.platon.metis.storage.dao.entity.TaskDataOptionPart;
import com.platon.metis.storage.service.TaskDataOptionPartService;
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
public class TaskDataOptionPartServiceImpl implements TaskDataOptionPartService {

    @Resource
    private TaskDataOptionPartMapper taskDataOptionPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void saveDataOption(String taskId, String dataPolicyOption) {
        String[] partArray = StrUtil.split(dataPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskDataOptionPart optionPart = new TaskDataOptionPart();
            optionPart.setTaskId(taskId);
            optionPart.setDataOptionPart(partArray[i]);
            taskDataOptionPartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public String getDataOption(String taskId) {
        List<TaskDataOptionPart> list = taskDataOptionPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getDataOptionPart());
        });
        return sb.toString();
    }

}
