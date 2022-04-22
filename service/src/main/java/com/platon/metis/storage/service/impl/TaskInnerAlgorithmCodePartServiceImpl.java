package com.platon.metis.storage.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.platon.metis.storage.dao.TaskInnerAlgorithmCodePartMapper;
import com.platon.metis.storage.dao.entity.TaskDataOptionPart;
import com.platon.metis.storage.dao.entity.TaskInnerAlgorithmCodePart;
import com.platon.metis.storage.service.TaskInnerAlgorithmCodePartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:33
 * @Version
 * @Desc
 */

@Service
@Slf4j
public class TaskInnerAlgorithmCodePartServiceImpl implements TaskInnerAlgorithmCodePartService {

    @Resource
    private TaskInnerAlgorithmCodePartMapper taskInnerAlgorithmCodePartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Override
    public void saveAlgorithmCode(String taskId, String algorithmCode, String algorithmCodeExtraParams) {
        String[] codeArray = StrUtil.split(algorithmCode, optionPartSize);
        String[] paramArray = StrUtil.split(algorithmCodeExtraParams, optionPartSize);
        for (int i = 0; i < codeArray.length || i < paramArray.length; i++) {
            TaskInnerAlgorithmCodePart optionPart = new TaskInnerAlgorithmCodePart();
            optionPart.setTaskId(taskId);
            if (codeArray.length >= i + 1) {//还没溢出
                optionPart.setAlgorithmCodePart(codeArray[i]);
            }
            if (paramArray.length >= i + 1) {//还没溢出
                optionPart.setAlgorithmCodeExtraParamsPart(paramArray[i]);
            }
            taskInnerAlgorithmCodePartMapper.insertSelective(optionPart);
        }
    }

    @Override
    public Pair<String,String> getAlgorithmCode(String taskId) {
        List<TaskInnerAlgorithmCodePart> list = taskInnerAlgorithmCodePartMapper.selectByTaskId(taskId);
        StringBuilder codeSb = new StringBuilder();
        StringBuilder paramSb = new StringBuilder();
        list.forEach(part -> {
            if(StrUtil.isNotBlank(part.getAlgorithmCodePart())){
                codeSb.append(part.getAlgorithmCodePart());
            }
            if(StrUtil.isNotBlank(part.getAlgorithmCodeExtraParamsPart())){
                paramSb.append(part.getAlgorithmCodeExtraParamsPart());
            }
        });
        return Pair.of(codeSb.toString(),paramSb.toString());
    }
}
