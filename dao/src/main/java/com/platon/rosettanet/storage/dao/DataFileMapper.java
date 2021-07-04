package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.DataFile;

import java.util.List;

public interface DataFileMapper {
    int deleteByPrimaryKey(String id);

    int insert(DataFile record);

    int insertSelective(DataFile record);

    DataFile selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(DataFile record);

    int updateByPrimaryKey(DataFile record);

    List<DataFile> listDataFile(String status);

    DataFile findByMetaDataId(String metaDataId);

    void deleteByMetaDataId(String metaDataId);

    void insertBatch(List<DataFile> dataFileList);
}