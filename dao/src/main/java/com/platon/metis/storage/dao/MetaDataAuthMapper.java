package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.MetaDataAuth;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MetaDataAuthMapper {

    int insertSelective(MetaDataAuth record);

    MetaDataAuth selectByPrimaryKey(String metaDataAuthId);

    int updateByPrimaryKeySelective(MetaDataAuth record);

    List<MetaDataAuth> syncMetaDataAuth(@Param("identityId") String identityId, @Param("lastUpdateAt") LocalDateTime lastUpdateAt, @Param("limit") long limit);
}