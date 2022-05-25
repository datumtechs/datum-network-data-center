package com.platon.metis.storage.service;


import com.platon.metis.storage.dao.entity.TaskPowerResourceOptions;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:29
 * @Version
 * @Desc
 */
public interface TaskPowerResourceOptionsService {
    void savePowerResourceOption(List<TaskPowerResourceOptions> powerResourceOptionsList);

    List<TaskPowerResourceOptions> getPowerResourceOption(String taskId);
}
