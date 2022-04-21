package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.MetaData;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataService {

    MetaData findByMetaDataId(String metaDataId);
    void insertMetaData(MetaData dataFile);
    List<MetaData> listDataFile(int status, LocalDateTime lastUpdatedAt, long limit);
    List<MetaData> syncDataFile(LocalDateTime lastUpdatedAt, long limit);
    List<MetaData> syncDataFileByIdentityId(String identityId, LocalDateTime lastUpdatedAt, long limit);
    void insertDataFile(List<MetaData> dataFileList);
    void updateStatus(String metaDataId, int status);

    /**
     * v0.4.0支持绑定合约地址
     */
    void update(MetaData metaData);

    List<MetaData> findByMetaDataIdList(List<String> metaDataIdList);
}
