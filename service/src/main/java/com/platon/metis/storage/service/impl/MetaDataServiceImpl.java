package com.platon.metis.storage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.platon.metis.storage.dao.MetaDataMapper;
import com.platon.metis.storage.dao.MetaDataOptionPartMapper;
import com.platon.metis.storage.dao.entity.MetaData;
import com.platon.metis.storage.dao.entity.MetaDataOptionPart;
import com.platon.metis.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MetaDataServiceImpl implements MetaDataService {

    @Autowired
    private MetaDataMapper metaDataMapper;

    @Autowired
    private MetaDataOptionPartMapper metaDataOptionPartMapper;

    @Value("${option-part.size}")
    private int optionPartSize;

    @Transactional
    @Override
    public void insertMetaData(MetaData dataFile) {
        metaDataMapper.insert(dataFile);
        //判断metaDataOption是否会过大，如果过大则分开存储，防止数据库字段存储不下
        saveMetaDataOption(dataFile.getMetaDataId(), dataFile.getMetaDataOption());
    }

    @Override
    public List<MetaData> listDataFile(int status, LocalDateTime lastUpdatedAt, long limit) {
        List<MetaData> metaDataList = metaDataMapper.listDataFile(status, lastUpdatedAt, limit);
        metaDataList.forEach(metaData -> {
            metaData.setMetaDataOption(getMetaDataOption(metaData.getMetaDataId()));
        });
        return metaDataList;
    }

    @Override
    public List<MetaData> syncDataFile(LocalDateTime lastUpdatedAt, long limit) {
        List<MetaData> metaDataList = metaDataMapper.syncDataFile(lastUpdatedAt, limit);
        metaDataList.forEach(metaData -> {
            metaData.setMetaDataOption(getMetaDataOption(metaData.getMetaDataId()));
        });
        return metaDataList;
    }

    @Override
    public List<MetaData> syncDataFileByIdentityId(String identityId, LocalDateTime lastUpdatedAt, long limit) {
        List<MetaData> metaDataList = metaDataMapper.syncDataFileByIdentityId(identityId, lastUpdatedAt, limit);
        metaDataList.forEach(metaData -> {
            metaData.setMetaDataOption(getMetaDataOption(metaData.getMetaDataId()));
        });
        return metaDataList;
    }


    @Override
    public void insertDataFile(List<MetaData> dataFileList) {
        metaDataMapper.insertBatch(dataFileList);
    }


    @Override
    public void updateStatus(String metaDataId, int status) {
        metaDataMapper.updateStatus(metaDataId, status);
        //metaDataColumnMapper.deleteByMetaDataId(metaDataId);
    }

    @Transactional
    @Override
    public void update(MetaData metaData) {
        //1.修改metadata信息
        metaDataMapper.updateByPrimaryKeySelective(metaData);
        //2.删除metadata列信息
        metaDataOptionPartMapper.deleteByMetaDataId(metaData.getMetaDataId());
        //3.新增metadata列信息
        saveMetaDataOption(metaData.getMetaDataId(), metaData.getMetaDataOption());
    }

    @Override
    public MetaData findByMetaDataId(String metaDataId) {
        MetaData metaData = metaDataMapper.selectByPrimaryKey(metaDataId);
        metaData.setMetaDataOption(getMetaDataOption(metaDataId));
        return metaData;
    }

    @Override
    public List<MetaData> findByMetaDataIdList(List<String> metaDataIdList) {
        if(metaDataIdList.isEmpty()){
            return new ArrayList<>();
        }
        List<MetaData> metaDataList = metaDataMapper.selectByMetaDataIdList(metaDataIdList);
        metaDataList.forEach(metaData -> {
            metaData.setMetaDataOption(getMetaDataOption(metaData.getMetaDataId()));
        });
        return metaDataList;
    }

    private String getMetaDataOption(String metaDataId) {
        List<MetaDataOptionPart> metaDataOptionParts = metaDataOptionPartMapper.selectByMetaDataId(metaDataId);
        StringBuilder sb = new StringBuilder();
        metaDataOptionParts.forEach(part -> {
            sb.append(part.getMetaDataOptionPart());
        });
        return sb.toString();
    }

    private void saveMetaDataOption(String metaDataId, String metaDataOption) {
        String[] partArray = StrUtil.split(metaDataOption, optionPartSize);
        for (int i = 0; i < partArray.length; i++) {
            MetaDataOptionPart optionPart = new MetaDataOptionPart();
            optionPart.setMetaDataId(metaDataId);
            optionPart.setMetaDataOptionPart(partArray[i]);
            metaDataOptionPartMapper.insert(optionPart);
        }
    }

}
