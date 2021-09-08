package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MetaDataColumnMapper {
    int deleteByPrimaryKey(@Param("metaDataId") String metaDataId, @Param("columnIdx") Integer columnIdx);

    int insert(MetaDataColumn record);

    int insertSelective(MetaDataColumn record);

    MetaDataColumn selectByPrimaryKey(@Param("metaDataId") String metaDataId, @Param("columnIdx") Integer columnIdx);

    int updateByPrimaryKeySelective(MetaDataColumn record);

    int updateByPrimaryKey(MetaDataColumn record);

    List<MetaDataColumn> listMetaDataColumn(String metaDataId);

    void insertBatch(List<MetaDataColumn> metaDataColumnList);

}