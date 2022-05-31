package com.platon.datum.storage.dao;

import com.platon.datum.storage.dao.entity.ScheduleServer;

public interface ScheduleServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(ScheduleServer record);

    int insertSelective(ScheduleServer record);

    ScheduleServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ScheduleServer record);

    int updateByPrimaryKey(ScheduleServer record);
}