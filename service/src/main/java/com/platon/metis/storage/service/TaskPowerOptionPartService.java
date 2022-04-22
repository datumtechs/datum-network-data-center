package com.platon.metis.storage.service;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:28
 * @Version
 * @Desc
 */
public interface TaskPowerOptionPartService {
    void savePowerOption(String taskId, String powerPolicyOption);

    String getPowerOption(String taskId);
}
