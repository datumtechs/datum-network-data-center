package com.platon.metis.storage.service;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:26
 * @Version
 * @Desc
 */
public interface TaskDataOptionPartService {
    void saveDataOption(String taskId, String dataPolicyOption);

    String getDataOption(String taskId);
}
