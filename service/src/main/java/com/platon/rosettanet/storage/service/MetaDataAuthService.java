package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.MetaDataAuth;
import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.dao.entity.TaskEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthService {
    int insert(MetaDataAuth metaDataAuth);
    void insert(List<MetaDataAuth> metaDataAuthList);

    int updateStatus(String metaDataAuthId, int status);

    List<MetaDataAuth> syncMetaDataAuth(String identityId, LocalDateTime lastUpdateAt);
}
