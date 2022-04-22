package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskDataFlowOptionPart;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskDataFlowOptionPartMapper {
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
    int insert(TaskDataFlowOptionPart record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskDataFlowOptionPart record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    TaskDataFlowOptionPart selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskDataFlowOptionPart record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskDataFlowOptionPart record);

    List<TaskDataFlowOptionPart> selectByTaskId(String taskId);
}