package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.rosettanet.storage.dao.entity.PowerServer;

import java.util.List;

public interface PowerServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(PowerServer record);

    int insertSelective(PowerServer record);

    PowerServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PowerServer record);

    int updateByPrimaryKey(PowerServer record);

    List<PowerServer> listPowerServer();

    /**
     * 只包含 各项 的sum
     * @param identityId
     * @return
     */
    PowerServer countPowerByOrgId(String identityId);

    List<OrgPowerTaskSummary> countPowerGroupByOrgId();
}