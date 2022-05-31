package com.platon.datum.storage.service.impl;

import com.platon.datum.storage.dao.OrgInfoMapper;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.service.OrgInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class OrgInfoServiceImpl implements OrgInfoService {

    @Autowired
    private OrgInfoMapper orgInfoMapper;

    @Override
    public void insert(OrgInfo orgInfo) {
        if(orgInfo == null){
            return;
        }
        orgInfoMapper.insert(orgInfo);
    }

    @Override
    public void insert(List<OrgInfo> orgInfoList) {
        if(orgInfoList == null || orgInfoList.isEmpty()){
            return;
        }
        orgInfoMapper.insertBatch(orgInfoList);
    }

    @Override
    public OrgInfo findByPK(String identityId) {
        return orgInfoMapper.selectByPrimaryKey(identityId);
    }

    @Override
    public List<OrgInfo> syncOrgInfo(LocalDateTime lastUpdatedAt, long limit) {
        return orgInfoMapper.syncOrgInfo(lastUpdatedAt, limit);
    }

    @Override
    public void update(OrgInfo orgInfo) {
        orgInfoMapper.updateByIdentityId(orgInfo, orgInfo.getIdentityId());
    }

    @Override
    public void updateStatus(String identityId, int status) {
        orgInfoMapper.updateStatus(identityId, status);
    }
}
