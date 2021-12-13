package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.DataFile;
import com.platon.metis.storage.dao.entity.MetaDataColumn;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataService {

    DataFile findByMetaDataId(String metaDataId);

    List<MetaDataColumn> listMetaDataColumn(String metaDataId);

    void insertMetaData(DataFile dataFile, List<MetaDataColumn> metaDataColumnList);

    List<DataFile> listDataFile(int status, LocalDateTime lastUpdatedAt, long limit);
    List<DataFile> syncDataFile(LocalDateTime lastUpdatedAt, long limit);
    void insertDataFile(List<DataFile> dataFileList);
    void insertMetaDataColumn(List<MetaDataColumn> metaDataColumnList);
    void updateStatus(String metaDataId, int status);
}
