package com.platon.metis.storage.dao.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleServer {
    private String id;

    private String identityId;

    private String internalIp;

    private String internalPort;

    private String status;

}