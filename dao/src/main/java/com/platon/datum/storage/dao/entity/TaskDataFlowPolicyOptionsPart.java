package com.platon.datum.storage.dao.entity;

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
    * 任务的数据流向策略的内容 (json字符串, 和 data_flow_policy_type 配套使用)
    */
@Getter
@Setter
@ToString
public class TaskDataFlowPolicyOptionsPart {
    private Integer id;

    /**
    * 关联的任务ID
    */
    private String taskId;

    /**
    * 数据片段
    */
    private String dataFlowPolicyOptionsPart;
}