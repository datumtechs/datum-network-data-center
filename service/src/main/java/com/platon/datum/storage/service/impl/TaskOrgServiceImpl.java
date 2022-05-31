package com.platon.datum.storage.service.impl;

import com.platon.datum.storage.dao.TaskOrgMapper;
import com.platon.datum.storage.dao.entity.TaskOrg;
import com.platon.datum.storage.service.TaskOrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 15:06
 * @Version
 * @Desc
 */

@Service
@Slf4j
public class TaskOrgServiceImpl implements TaskOrgService {

    @Resource
    private TaskOrgMapper taskOrgMapper;

    @Override
    public void saveTaskOrg(List<TaskOrg> taskOrgList) {
        if(taskOrgList == null || taskOrgList.isEmpty()){
            return;
        }
        taskOrgMapper.insertList(taskOrgList);
    }

    @Override
    public List<TaskOrg> findTaskOrgList(String taskId) {
        return taskOrgMapper.selectAllOrgByTaskId(taskId);
    }
}
