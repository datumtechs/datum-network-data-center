package com.platon.metis.storage.service;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:26
 * @Version
 * @Desc
 */
public interface TaskDataOptionPartService {
    void saveDataOption(String taskId, List<String> dataPolicyOptionList);

    List<String> getDataOption(String taskId);
}
