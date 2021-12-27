package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.OrgInfo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgInfoMapper {
    int deleteByPrimaryKey(String identityId);

    int insert(OrgInfo record);

    OrgInfo selectByPrimaryKey(String identityId);

    int updateByPrimaryKey(OrgInfo record);
    int updateStatus(@Param("identityId") String identityId, @Param("status") int status);

    OrgInfo findByMetaDataId(String metaDataId);

    List<OrgInfo> syncOrgInfo(@Param("lastUpdatedAt") LocalDateTime lastUpdatedAt, @Param("limit")long limit);

    int insertBatch(List<OrgInfo> orgInfoList);
}