package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.MetaDataAuth;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthService {
    int insertSelective(MetaDataAuth metaDataAuth);

    void insert(List<MetaDataAuth> metaDataAuthList);

    int updateStatus(String metaDataAuthId, int status);

    int updateSelective(MetaDataAuth metaDataAuth);

    List<MetaDataAuth> syncMetaDataAuth(String identityId, LocalDateTime lastUpdateAt);

    MetaDataAuth findByPK(String metaDataAuthId);
}
