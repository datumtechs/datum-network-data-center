package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.DataFile;
import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;

import java.util.List;

public interface MetaDataService {

    DataFile findByMetaDataId(String metaDataId);

    List<MetaDataColumn> listMetaDataColumn(String metaDataId);

    void insertMetaData(DataFile dataFile, List<MetaDataColumn> metaDataColumnList);

    List<DataFile> listDataFile(String status);

    void insertDataFile(List<DataFile> dataFileList);
    void insertMetaDataColumn(List<MetaDataColumn> metaDataColumnList);
    void deleteByMetaDataId(String metaDataId);
}
