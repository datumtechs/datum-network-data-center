package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.platon.metis.storage.dao.TaskDataFlowOptionPartMapper;
import com.platon.metis.storage.dao.entity.TaskDataFlowOptionPart;
import com.platon.metis.storage.service.BaseService;
import com.platon.metis.storage.service.TaskDataFlowOptionPartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:15
 * @Version
 * @Desc
 */

@Service
@Slf4j
public class TaskDataFlowOptionPartServiceImpl extends BaseService implements TaskDataFlowOptionPartService {

    @Resource
    private TaskDataFlowOptionPartMapper taskDataFlowOptionPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;


    @Override
    public void saveDataFlowOption(String taskId, List<String> dataFlowPolicyOptionList) {
        String dataFlowPolicyOption = list2string(dataFlowPolicyOptionList);
        String[] partArray = StrUtil.split(dataFlowPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskDataFlowOptionPart optionPart = new TaskDataFlowOptionPart();
            optionPart.setTaskId(taskId);
            optionPart.setDataFlowOptionPart(partArray[i]);
            taskDataFlowOptionPartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public List<String> getDataFlowOption(String taskId) {
        List<TaskDataFlowOptionPart> list = taskDataFlowOptionPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getDataFlowOptionPart());
        });
        return string2list(sb.toString());
    }


}
