package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.MetaDataAuth;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthMapper {
    int deleteByPrimaryKey(String metaDataAuthId);

    int insert(MetaDataAuth record);
    int insertSelective(MetaDataAuth record);

    MetaDataAuth selectByPrimaryKey(String metaDataAuthId);

    int updateByPrimaryKey(MetaDataAuth record);

    int updateByPrimaryKeySelective(MetaDataAuth record);

    void insertBatch(List<MetaDataAuth> metaDataAuthList);

    int updateStatus(@Param("metaDataAuthId") String metaDataAuthId, @Param("status") int status);

    List<MetaDataAuth> syncMetaDataAuth(@Param("identityId") String identityId, @Param("lastUpdateAt") LocalDateTime lastUpdateAt, @Param("limit") long limit);
}