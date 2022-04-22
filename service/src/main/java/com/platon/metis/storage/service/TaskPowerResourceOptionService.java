package com.platon.metis.storage.service;


import com.platon.metis.storage.dao.entity.TaskPowerResourceOption;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:29
 * @Version
 * @Desc
 */
public interface TaskPowerResourceOptionService {
    void savePowerResourceOption(List<TaskPowerResourceOption> powerResourceOptionsList);

    List<TaskPowerResourceOption> getPowerResourceOption(String taskId);
}
