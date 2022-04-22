package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskPowerResourceOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/22 16:11
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskPowerResourceOptionMapper {
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
    int insert(TaskPowerResourceOption record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskPowerResourceOption record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    TaskPowerResourceOption selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskPowerResourceOption record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskPowerResourceOption record);

    void insertList(@Param("optionList") List<TaskPowerResourceOption> list);

    List<TaskPowerResourceOption> selectByTaskId(String taskId);
}