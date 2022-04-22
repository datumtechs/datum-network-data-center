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
    * 任务的数据提供方选择数据策略的内容 (json字符串, 和 data_policy_type 配套使用)
    */
@Getter
@Setter
@ToString
public class TaskDataOptionPart {
    /**
    * 主键ID
    */
    private Integer id;

    /**
    * 关联的任务ID
    */
    private String taskId;

    /**
    * 分片后的信息
    */
    private String dataOptionPart;
}