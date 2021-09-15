package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.PowerServerMapper;
import com.platon.rosettanet.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.rosettanet.storage.dao.entity.PowerServer;
import com.platon.rosettanet.storage.service.PowerServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public void insert(List<PowerServer> powerServerList) {
        powerServerMapper.insertBatch(powerServerList);
    }

    @Override
    public int updateByPrimaryKeySelective(PowerServer powerServer) {
        return powerServerMapper.updateByPrimaryKeySelective(powerServer);
    }

    @Override
    public int updateStatus(String powerId, int status) {
        return powerServerMapper.updateStatus(powerId, status);
    }

    @Override
    public List<PowerServer> syncPowerServer(LocalDateTime lastUpdatedAt) {
        return powerServerMapper.syncPowerServer(lastUpdatedAt);
    }

    @Override
    public PowerServer sumPowerByOrgId(String identityId) {
        return powerServerMapper.countPowerByOrgId(identityId);
    }

    @Override
    public List<OrgPowerTaskSummary> listPowerSummaryGroupByOrgId() {
        return powerServerMapper.listPowerSummaryGroupByOrgId();
    }

    @Override
    public OrgPowerTaskSummary getPowerSummaryByOrgId(String identityId) {
        return powerServerMapper.getPowerSummaryByOrgId(identityId);
    }


}
