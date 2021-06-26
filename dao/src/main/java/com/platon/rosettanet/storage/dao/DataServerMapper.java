package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.DataServer;

public interface DataServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(DataServer record);

    int insertSelective(DataServer record);

    DataServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(DataServer record);

    int updateByPrimaryKey(DataServer record);
}