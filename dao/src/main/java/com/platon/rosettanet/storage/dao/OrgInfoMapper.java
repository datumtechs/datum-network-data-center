package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.OrgInfo;

import java.util.List;

public interface OrgInfoMapper {
    int deleteByPrimaryKey(String identityId);

    int insert(OrgInfo record);

    OrgInfo selectByPrimaryKey(String identityId);

    int updateByPrimaryKeySelective(OrgInfo record);

    int updateByPrimaryKey(OrgInfo record);

    OrgInfo findByMetaDataId(String metaDataId);

    List<OrgInfo> listOrgInfo();

    int insertBatch(List<OrgInfo> orgInfoList);
}