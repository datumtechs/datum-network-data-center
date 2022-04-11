package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.MetaData;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataMapper {
    int deleteByPrimaryKey(String metaDataId);

    /**
     * 插入数据
     * @param record
     * @return
     */
    int insert(MetaData record);

//    int insertSelective(MetaData record);

    MetaData selectByPrimaryKey(String metaDataId);

//    int updateByPrimaryKeySelective(MetaData record);

//    int updateByPrimaryKey(MetaData record);

    List<MetaData> listDataFile(@Param("status") int status, @Param("lastUpdatedAt") LocalDateTime lastUpdatedAt,@Param("limit") long limit);

    void insertBatch(List<MetaData> dataFileList);

    List<MetaData> syncDataFile(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt,@Param("limit") long limit);
    List<MetaData> syncDataFileByIdentityId(@Param("identityId") String identityId, @Param("lastUpdatedAt") LocalDateTime lastUpdatedAt,@Param("limit") long limit);
    void updateStatus(@Param("metaDataId")String metaDataId, @Param("status")int status);

    void updateByPrimaryKeySelective(MetaData metaData);
}