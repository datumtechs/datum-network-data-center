package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.MetaDataOptionPart;

import java.util.List;

public interface MetaDataOptionPartMapper {
    List<MetaDataOptionPart> selectByMetaDataId(String metaDataId);

    int deleteByMetaDataId(String metaDataId);

    int insert(MetaDataOptionPart metaDataOption);
}