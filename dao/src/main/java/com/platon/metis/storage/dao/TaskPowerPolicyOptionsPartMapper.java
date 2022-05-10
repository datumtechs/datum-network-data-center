package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskPowerPolicyOptionsPart;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskPowerPolicyOptionsPartMapper {
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
    int insert(TaskPowerPolicyOptionsPart record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskPowerPolicyOptionsPart record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    TaskPowerPolicyOptionsPart selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskPowerPolicyOptionsPart record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskPowerPolicyOptionsPart record);

    List<TaskPowerPolicyOptionsPart> selectByTaskId(String taskId);
}