package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.DataFileMapper;
import com.platon.rosettanet.storage.dao.MetaDataColumnMapper;
import com.platon.rosettanet.storage.dao.entity.DataFile;
import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;
import com.platon.rosettanet.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<DataFile> listDataFile(String status) {
        return dataFileMapper.listDataFile(status);
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
    public void deleteByMetaDataId(String metaDataId) {
        dataFileMapper.deleteByMetaDataId(metaDataId);
        //metaDataColumnMapper.deleteByMetaDataId(metaDataId);
    }

    @Override
    public DataFile findByMetaDataId(String metaDataId) {
        return dataFileMapper.findByMetaDataId(metaDataId);
    }

    @Override
    public List<MetaDataColumn> listMetaDataColumn(String metaDataId) {
        return metaDataColumnMapper.listMetaDataColumn(metaDataId);
    }
}
