package com.platon.metis.storage.service;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:27
 * @Version
 * @Desc
 */
public interface TaskDataFlowOptionPartService {
    void saveDataFlowOption(String taskId, String dataFlowPolicyOption);

    String getDataFlowOption(String taskId);
}
