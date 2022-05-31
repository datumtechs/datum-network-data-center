package com.platon.datum.storage.service;

import com.platon.datum.storage.dao.entity.TaskOrg;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:21
 * @Version
 * @Desc
 */
public interface TaskOrgService {
    void saveTaskOrg(List<TaskOrg> taskOrgList);

    List<TaskOrg> findTaskOrgList(String taskId);
}
