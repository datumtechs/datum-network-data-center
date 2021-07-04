package com.platon.rosettanet.storage.dao;

import com.platon.rosettanet.storage.dao.entity.DataServer;

import java.util.List;

public interface DataServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(DataServer dataServer);

    void insertBatch(List<DataServer> dataServerList);

    int insertSelective(DataServer dataServer);

    DataServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(DataServer dataServer);

    int updateByPrimaryKey(DataServer dataServer);
}