package com.platon.datum.storage.dao;

import com.platon.datum.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.datum.storage.dao.entity.PowerServer;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PowerServerMapper {
    int deleteByPrimaryKey(String id);

    int insertSelective(PowerServer record);

    PowerServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PowerServer record);

    List<PowerServer> syncPowerServer(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt, @Param("limit") long limit);

    /**
     * 只包含 各项 的sum
     *
     * @param identityId
     * @return
     */
    PowerServer countPowerByOrgId(String identityId);

    List<OrgPowerTaskSummary> listPowerSummaryGroupByOrgId();

    void insertBatch(List<PowerServer> powerServerList);

    int updateStatus(@Param("id") String powerId, @Param("status") int status);

    OrgPowerTaskSummary getPowerSummaryByOrgId(@Param("identityId") String identityId);
}