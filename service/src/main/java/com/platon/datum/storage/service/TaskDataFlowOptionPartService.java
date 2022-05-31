package com.platon.datum.storage.service;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:27
 * @Version
 * @Desc
 */
public interface TaskDataFlowOptionPartService {
    void saveDataFlowOption(String taskId, List<String> dataFlowPolicyOptionList);

    List<String> getDataFlowOption(String taskId);
}
