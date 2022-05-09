package com.platon.metis.storage.service;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/5/9 11:20
 * @Version
 * @Desc
 */


public interface TaskReceiverOptionService {

    void saveReceiverOption(String taskId, List<String> receiverPolicyOptionList);

    List<String> getReceiverOption(String taskId);
}
