package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.OrgInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgInfoService {
    int insert(OrgInfo orgInfo);
    int insert(List<OrgInfo> orgInfoList);

    OrgInfo findByPK(String identityId);

    List<OrgInfo> syncOrgInfo(LocalDateTime lastUpdatedAt, long limit);

    int update(OrgInfo orgInfo);

    int updateStatus(String identityId, int status);
}
