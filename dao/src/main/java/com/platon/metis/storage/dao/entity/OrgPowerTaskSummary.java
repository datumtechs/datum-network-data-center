package com.platon.metis.storage.dao.entity;

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

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getUsedCore() {
        return usedCore;
    }

    public void setUsedCore(int usedCore) {
        this.usedCore = usedCore;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setUsedBandwidth(long usedBandwidth) {
        this.usedBandwidth = usedBandwidth;
    }

    public int getPowerTaskCount() {
        return powerTaskCount;
    }

    public void setPowerTaskCount(int powerTaskCount) {
        this.powerTaskCount = powerTaskCount;
    }
}
