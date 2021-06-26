package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.PowerServerMapper;
import com.platon.rosettanet.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.rosettanet.storage.dao.entity.PowerServer;
import com.platon.rosettanet.storage.service.PowerServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class PowerServerServiceImpl implements PowerServerService {
    @Autowired
    private PowerServerMapper powerServerMapper;

    @Override
    public int insert(PowerServer powerServer) {
        return powerServerMapper.insert(powerServer);
    }

    @Override
    public int updateByPrimaryKeySelective(PowerServer powerServer) {
        return powerServerMapper.updateByPrimaryKeySelective(powerServer);
    }

    @Override
    public int deleteByPK(String powerId) {
        return powerServerMapper.deleteByPrimaryKey(powerId);
    }

    @Override
    public List<PowerServer> listPowerServer() {
        return powerServerMapper.listPowerServer();
    }

    @Override
    public PowerServer countPowerByOrgId(String identityId) {
        return powerServerMapper.countPowerByOrgId(identityId);
    }

    @Override
    public List<OrgPowerTaskSummary> countPowerGroupByOrgId() {
        return powerServerMapper.countPowerGroupByOrgId();
    }


}
