package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskReceiverPolicyOptionsPart;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/5/9 11:38
 * @Version
 * @Desc ******************************
 */
public interface TaskReceiverPolicyOptionsPartMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(TaskReceiverPolicyOptionsPart record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskReceiverPolicyOptionsPart record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    TaskReceiverPolicyOptionsPart selectByPrimaryKey(Integer id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskReceiverPolicyOptionsPart record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskReceiverPolicyOptionsPart record);

    List<TaskReceiverPolicyOptionsPart> selectByTaskId(String taskId);
}