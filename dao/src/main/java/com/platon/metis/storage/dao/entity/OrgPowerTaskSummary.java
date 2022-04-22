package com.platon.metis.storage.dao.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrgPowerTaskSummary {
    private String identityId;
    private String orgName;
    private int core;
    private long memory;
    private long bandwidth;
    private int usedCore;
    private long usedMemory;
    private long usedBandwidth;
    private int powerTaskCount;
}
