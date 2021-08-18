package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.OrgInfoMapper;
import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.service.OrgInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class OrgInfoServiceImpl implements OrgInfoService {

    @Autowired
    private OrgInfoMapper orgInfoMapper;

    @Override
    public int insert(OrgInfo orgInfo) {
        return orgInfoMapper.insert(orgInfo);
    }

    @Override
    public int insert(List<OrgInfo> orgInfoList) {
        return orgInfoMapper.insertBatch(orgInfoList);
    }

    @Override
    public OrgInfo findByPK(String identityId) {
        return orgInfoMapper.selectByPrimaryKey(identityId);
    }

    @Override
    public OrgInfo findByMetaDataId(String metaDataId) {
        return orgInfoMapper.findByMetaDataId(metaDataId);
    }

    @Override
    public List<OrgInfo> listOrgInfo() {
        return orgInfoMapper.listOrgInfo();
    }

    @Override
    public int deleteByPK(String identityId) {
        return orgInfoMapper.deleteByPrimaryKey(identityId);
    }

    @Override
    public int update(OrgInfo orgInfo) {
        return orgInfoMapper.updateByPrimaryKey(orgInfo);
    }
}
