package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.DataFileMapper;
import com.platon.metis.storage.dao.MetaDataColumnMapper;
import com.platon.metis.storage.dao.entity.DataFile;
import com.platon.metis.storage.dao.entity.MetaDataColumn;
import com.platon.metis.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MetaDataServiceImpl implements MetaDataService {

    @Autowired
    private DataFileMapper dataFileMapper;

    @Autowired
    private MetaDataColumnMapper metaDataColumnMapper;

    @Override
    public void insertMetaData(DataFile dataFile, List<MetaDataColumn> metaDataColumnList) {
        dataFileMapper.insert(dataFile);
        metaDataColumnMapper.insertBatch(metaDataColumnList);
    }

    @Override
    public List<DataFile> listDataFile(int status, LocalDateTime lastUpdatedAt, long limit) {
        return dataFileMapper.listDataFile(status, lastUpdatedAt, limit);
    }

    @Override
    public List<DataFile> syncDataFile(LocalDateTime lastUpdatedAt, long limit) {
        return dataFileMapper.syncDataFile(lastUpdatedAt, limit);
    }

    @Override
    public List<DataFile> syncDataFileByIdentityId(String identityId, LocalDateTime lastUpdatedAt, long limit) {
        return dataFileMapper.syncDataFileByIdentityId(identityId, lastUpdatedAt, limit);
    }


    @Override
    public void insertDataFile(List<DataFile> dataFileList) {
        dataFileMapper.insertBatch(dataFileList);
    }

    @Override
    public void insertMetaDataColumn(List<MetaDataColumn> metaDataColumnList) {
        metaDataColumnMapper.insertBatch(metaDataColumnList);
    }

    @Override
    public void updateStatus(String metaDataId, int status) {
        dataFileMapper.updateStatus(metaDataId, status);
        //metaDataColumnMapper.deleteByMetaDataId(metaDataId);
    }

    @Override
    public DataFile findByMetaDataId(String metaDataId) {
        return dataFileMapper.selectByPrimaryKey(metaDataId);
    }

    @Override
    public List<MetaDataColumn> listMetaDataColumn(String metaDataId) {
        return metaDataColumnMapper.listMetaDataColumn(metaDataId);
    }
}
