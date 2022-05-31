package com.platon.datum.storage.service;

import com.platon.datum.storage.dao.entity.OrgInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgInfoService {
    void insert(OrgInfo orgInfo);
    void insert(List<OrgInfo> orgInfoList);

    OrgInfo findByPK(String identityId);

    List<OrgInfo> syncOrgInfo(LocalDateTime lastUpdatedAt, long limit);

    void update(OrgInfo orgInfo);

    void updateStatus(String identityId, int status);
}