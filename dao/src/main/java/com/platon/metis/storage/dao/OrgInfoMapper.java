package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.OrgInfo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgInfoMapper {

    int insert(OrgInfo record);

    OrgInfo selectByPrimaryKey(String identityId);

    int updateByIdentityId(@Param("updated") OrgInfo updated, @Param("identityId") String identityId);

    int updateStatus(@Param("identityId") String identityId, @Param("status") int status);

    List<OrgInfo> syncOrgInfo(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt, @Param("limit") long limit);

    int insertBatch(List<OrgInfo> orgInfoList);
}