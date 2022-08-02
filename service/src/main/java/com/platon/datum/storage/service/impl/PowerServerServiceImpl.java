package com.platon.datum.storage.service.impl;

import com.platon.datum.storage.dao.PowerServerMapper;
import com.platon.datum.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.datum.storage.dao.entity.PowerServer;
import com.platon.datum.storage.service.PowerServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class PowerServerServiceImpl implements PowerServerService {
    @Resource
    private PowerServerMapper powerServerMapper;

    @Override
    public int insert(PowerServer powerServer) {
        return powerServerMapper.insertSelective(powerServer);
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
    public List<PowerServer> syncPowerServer(LocalDateTime lastUpdatedAt, long limit) {
        return powerServerMapper.syncPowerServer(lastUpdatedAt, limit);
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
