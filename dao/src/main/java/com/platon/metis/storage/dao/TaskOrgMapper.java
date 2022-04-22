package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskOrg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version 
 * @Desc 
 *******************************
 */
public interface TaskOrgMapper {
    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(TaskOrg record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskOrg record);

    void insertList(@Param("taskOrgList") List<TaskOrg> taskOrgList);

    List<TaskOrg> selectAllOrgByTaskId(String taskId);
}