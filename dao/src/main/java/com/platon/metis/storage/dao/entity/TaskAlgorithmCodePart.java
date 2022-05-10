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
    * 任务内置算法代码
    */
@Getter
@Setter
@ToString
public class TaskAlgorithmCodePart {
    private Integer id;

    /**
    * 关联的任务ID
    */
    private String taskId;

    /**
    * 内置算法代码 (使用内置算法, 在不使用算法市场前提下用)
    */
    private String algorithmCodePart;

    /**
    * 内置算法的额外超参 (使用内置算法, 内置算法的额外超参数 json 字符串, 内容可为""空字符串, 根据算法来)
    */
    private String algorithmCodeExtraParamsPart;
}