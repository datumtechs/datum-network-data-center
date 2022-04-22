package com.platon.metis.storage.service;

import cn.hutool.core.lang.Pair;

/**
 * @Author liushuyu
 * @Date 2022/4/22 14:28
 * @Version
 * @Desc
 */
public interface TaskInnerAlgorithmCodePartService {
    void saveAlgorithmCode(String taskId, String algorithmCode, String algorithmCodeExtraParams);

    Pair<String,String> getAlgorithmCode(String taskId);
}
