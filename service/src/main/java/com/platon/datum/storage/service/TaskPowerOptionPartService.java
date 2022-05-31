package com.platon.datum.storage.service;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:28
 * @Version
 * @Desc
 */
public interface TaskPowerOptionPartService {
    void savePowerOption(String taskId, List<String> powerPolicyOptionList);

    List<String> getPowerOption(String taskId);
}
