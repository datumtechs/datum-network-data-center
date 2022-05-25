package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskPowerResourceOptions;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/22 16:11
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskPowerResourceOptionsMapper {
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
    int insert(TaskPowerResourceOptions record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskPowerResourceOptions record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    TaskPowerResourceOptions selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskPowerResourceOptions record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskPowerResourceOptions record);

    void insertList(@Param("optionList") List<TaskPowerResourceOptions> list);

    List<TaskPowerResourceOptions> selectByTaskId(String taskId);
}