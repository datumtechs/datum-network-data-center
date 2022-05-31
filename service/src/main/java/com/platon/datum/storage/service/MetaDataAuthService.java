package com.platon.datum.storage.service;

import com.platon.datum.storage.dao.entity.MetaDataAuth;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthService {
    void insertSelective(MetaDataAuth metaDataAuth);

    void updateSelective(MetaDataAuth metaDataAuth);

    List<MetaDataAuth> syncMetaDataAuth(String identityId, LocalDateTime lastUpdateAt, long limit);

    MetaDataAuth findByPK(String metaDataAuthId);
}
