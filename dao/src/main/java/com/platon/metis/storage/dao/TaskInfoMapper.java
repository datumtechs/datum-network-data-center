package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.TaskInfo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version
 * @Desc ******************************
 */
public interface TaskInfoMapper {
    /**
     * delete by primary key
     *
     * @param taskId primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(String taskId);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(TaskInfo record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(TaskInfo record);

    /**
     * select by primary key
     *
     * @param taskId primary key
     * @return object by primary key
     */
    TaskInfo selectByPrimaryKey(String taskId);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(TaskInfo record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(TaskInfo record);

    List<TaskInfo> syncTaskInfo(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt, @Param("limit") long limit);

    List<TaskInfo> listTaskInfoByIdentityId(@Param("identityId") String identityId, @Param("lastUpdatedAt") LocalDateTime lastUpdatedAt, @Param("limit") long limit);

    List<TaskInfo> listTaskInfoByTaskIds(@Param("taskIdList") List<String> taskIdList);
}