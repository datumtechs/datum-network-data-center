package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.metis.storage.dao.TaskReceiverOptionPartMapper;
import com.platon.metis.storage.dao.entity.TaskReceiverOptionPart;
import com.platon.metis.storage.service.BaseService;
import com.platon.metis.storage.service.TaskReceiverOptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/5/9 11:22
 * @Version
 * @Desc
 */

@Service
public class TaskReceiverOptionServiceImpl extends BaseService implements TaskReceiverOptionService {

    @Resource
    private TaskReceiverOptionPartMapper taskReceiverOptionPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void saveReceiverOption(String taskId, List<String> receiverPolicyOptionList) {
        String receiverPolicyOption = list2string(receiverPolicyOptionList);
        String[] partArray = StrUtil.split(receiverPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskReceiverOptionPart optionPart = new TaskReceiverOptionPart();
            optionPart.setTaskId(taskId);
            optionPart.setReceiverOptionPart(partArray[i]);
            taskReceiverOptionPartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public List<String> getReceiverOption(String taskId) {
        List<TaskReceiverOptionPart> list = taskReceiverOptionPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getReceiverOptionPart());
        });
        return string2list(sb.toString());
    }
}
