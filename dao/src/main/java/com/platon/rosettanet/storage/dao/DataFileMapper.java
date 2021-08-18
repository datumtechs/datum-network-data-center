package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.DataFile;

import java.util.List;

public interface DataFileMapper {
    int deleteByPrimaryKey(String metaDataId);

    int insert(DataFile record);

    int insertSelective(DataFile record);

    DataFile selectByPrimaryKey(String metaDataId);

    int updateByPrimaryKeySelective(DataFile record);

    int updateByPrimaryKey(DataFile record);

    List<DataFile> listDataFile(String status);

    void insertBatch(List<DataFile> dataFileList);
}