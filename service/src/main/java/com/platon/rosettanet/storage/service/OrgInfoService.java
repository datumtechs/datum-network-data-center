package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.OrgInfo;

import java.util.List;

public interface OrgInfoService {
    int insert(OrgInfo orgInfo);
    int insert(List<OrgInfo> orgInfoList);

    OrgInfo findByPK(String identityId);

    OrgInfo findByMetaDataId(String metaDataId);

    List<OrgInfo> listOrgInfo();

    int deleteByPK(String identityId);

    int update(OrgInfo orgInfo);
}
