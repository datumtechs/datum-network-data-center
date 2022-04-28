package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.MetaDataAuth;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthService {
    int insertSelective(MetaDataAuth metaDataAuth);

    int updateSelective(MetaDataAuth metaDataAuth);

    List<MetaDataAuth> syncMetaDataAuth(String identityId, LocalDateTime lastUpdateAt, long limit);

    MetaDataAuth findByPK(String metaDataAuthId);
}
