package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.dao.MetaDataAuthMapper;
import com.platon.metis.storage.dao.entity.MetaDataAuth;
import com.platon.metis.storage.service.MetaDataAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@Transactional
public class MetaDataAuthServiceImpl implements MetaDataAuthService {
    @Autowired
    private MetaDataAuthMapper metaDataAuthMapper;

    @Override
    public int insertSelective(MetaDataAuth metaDataAuth) {
        return metaDataAuthMapper.insertSelective(metaDataAuth);
    }

    @Override
    public void insert(List<MetaDataAuth> metaDataAuthList) {
        metaDataAuthMapper.insertBatch(metaDataAuthList);
    }

    @Override
    public int updateSelective(MetaDataAuth metaDataAuth) {
        return metaDataAuthMapper.updateByPrimaryKeySelective(metaDataAuth);
    }

    @Override
    public List<MetaDataAuth> syncMetaDataAuth(String identityId, LocalDateTime lastUpdateAt, long limit) {
        return metaDataAuthMapper.syncMetaDataAuth(identityId, lastUpdateAt, limit);
    }

    @Override
    public MetaDataAuth findByPK(String metaDataAuthId) {
        return metaDataAuthMapper.selectByPrimaryKey(metaDataAuthId);
    }
}
