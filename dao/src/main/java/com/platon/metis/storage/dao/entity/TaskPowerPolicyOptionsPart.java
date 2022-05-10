package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version 
 * @Desc 
 *******************************
 */
/**
    * 任务的算力提供方选择算力策略的内容 (json字符串, 和 power_policy_type 配套使用)
    */
@Getter
@Setter
@ToString
public class TaskPowerPolicyOptionsPart {
    private Integer id;

    /**
    * 关联的任务ID
    */
    private String taskId;

    /**
    * 数据片段
    */
    private String powerPolicyOptionsPart;
}