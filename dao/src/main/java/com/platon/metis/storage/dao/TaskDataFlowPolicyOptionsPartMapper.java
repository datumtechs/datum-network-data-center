package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskDataFlowPolicyOptionsPart;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskDataFlowPolicyOptionsPartMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(TaskDataFlowPolicyOptionsPart record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskDataFlowPolicyOptionsPart record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    TaskDataFlowPolicyOptionsPart selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskDataFlowPolicyOptionsPart record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskDataFlowPolicyOptionsPart record);

    List<TaskDataFlowPolicyOptionsPart> selectByTaskId(String taskId);
}