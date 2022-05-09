package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author juzix
 * @Date 2022/5/9 11:38
 * @Version 
 * @Desc 
 *******************************
 */
/**
    * 任务的接收方选择策略的内容 (json字符串数组, 和 receiver_policy_types 配套使用, 使用数组的原因是 可以支持单个或者多个数目的策略)
    */
@Getter
@Setter
@ToString
public class TaskReceiverOptionPart {
    private Integer id;

    /**
    * 关联的任务ID
    */
    private String taskId;

    /**
    * 数据片段
    */
    private String receiverOptionPart;
}