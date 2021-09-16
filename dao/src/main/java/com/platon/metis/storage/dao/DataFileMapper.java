package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.DataFile;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DataFileMapper {
    int deleteByPrimaryKey(String metaDataId);

    int insert(DataFile record);

    int insertSelective(DataFile record);

    DataFile selectByPrimaryKey(String metaDataId);

    int updateByPrimaryKeySelective(DataFile record);

    int updateByPrimaryKey(DataFile record);

    List<DataFile> listDataFile(@Param("status") int status);

    void insertBatch(List<DataFile> dataFileList);

    List<DataFile> syncDataFile(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt);

    void updateStatus(@Param("metaDataId")String metaDataId, @Param("status")int status);
}