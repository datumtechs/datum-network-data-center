package com.platon.rosettanet.storage.dao.entity;

public class OrgInfo {
    private String identityId;

    private String identityType;

    private String orgName;

    private String status;

    private Long accumulativeMemory;

    private Integer accumulativeCore;

    private Long accumulativeBandwidth;

    private Integer accumulativePowerTaskCount;

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAccumulativeMemory() {
        return accumulativeMemory;
    }

    public void setAccumulativeMemory(Long accumulativeMemory) {
        this.accumulativeMemory = accumulativeMemory;
    }

    public Integer getAccumulativeCore() {
        return accumulativeCore;
    }

    public void setAccumulativeCore(Integer accumulativeCore) {
        this.accumulativeCore = accumulativeCore;
    }

    public Long getAccumulativeBandwidth() {
        return accumulativeBandwidth;
    }

    public void setAccumulativeBandwidth(Long accumulativeBandwidth) {
        this.accumulativeBandwidth = accumulativeBandwidth;
    }

    public Integer getAccumulativePowerTaskCount() {
        return accumulativePowerTaskCount;
    }

    public void setAccumulativePowerTaskCount(Integer accumulativePowerTaskCount) {
        this.accumulativePowerTaskCount = accumulativePowerTaskCount;
    }
}