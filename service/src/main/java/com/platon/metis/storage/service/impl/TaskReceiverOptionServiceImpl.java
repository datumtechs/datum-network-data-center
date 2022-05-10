package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.platon.metis.storage.dao.TaskReceiverPolicyOptionsPartMapper;
import com.platon.metis.storage.dao.entity.TaskReceiverPolicyOptionsPart;
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
    private TaskReceiverPolicyOptionsPartMapper taskReceiverPolicyOptionsPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void saveReceiverOption(String taskId, List<String> receiverPolicyOptionList) {
        String receiverPolicyOption = list2string(receiverPolicyOptionList);
        String[] partArray = StrUtil.split(receiverPolicyOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            TaskReceiverPolicyOptionsPart optionPart = new TaskReceiverPolicyOptionsPart();
            optionPart.setTaskId(taskId);
            optionPart.setReceiverPolicyOptionsPart(partArray[i]);
            taskReceiverPolicyOptionsPartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public List<String> getReceiverOption(String taskId) {
        List<TaskReceiverPolicyOptionsPart> list = taskReceiverPolicyOptionsPartMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        list.forEach(part -> {
            sb.append(part.getReceiverPolicyOptionsPart());
        });
        return string2list(sb.toString());
    }
}
