package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.rosettanet.storage.dao.entity.PowerServer;

import java.util.List;

public interface PowerServerService {
    int insert(PowerServer powerServer);

    void insert(List<PowerServer> powerServerList);


    int updateByPrimaryKeySelective(PowerServer record);

    int deleteByPK(String powerId);

    List<PowerServer> listPowerServer();

    /**
     * 只包含 各项 的sum
     * @param identityId
     * @return
     */
    PowerServer countPowerByOrgId(String identityId);

    /**
     *
     * List<Map<String, Object>>是算力server列表
     * 其中Map<String, Object> 是一行，key是column_name
     * @return
     */
    List<OrgPowerTaskSummary> countPowerGroupByOrgId();
}
