package com.platon.rosettanet.storage.dao.entity;

public class OrgInfo {
    private String identityId;

    private String identityType;

    private String nodeId;

    private String orgName;

    private String status;


    private Integer accumulativePowerTaskCount;
    private Integer accumulativeDataTaskCount;
    private Integer accumulativeDataFileCount;

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public Integer getAccumulativePowerTaskCount() {
        return accumulativePowerTaskCount;
    }

    public void setAccumulativePowerTaskCount(Integer accumulativePowerTaskCount) {
        this.accumulativePowerTaskCount = accumulativePowerTaskCount;
    }

    public Integer getAccumulativeDataTaskCount() {
        return accumulativeDataTaskCount;
    }

    public void setAccumulativeDataTaskCount(Integer accumulativeDataTaskCount) {
        this.accumulativeDataTaskCount = accumulativeDataTaskCount;
    }

    public Integer getAccumulativeDataFileCount() {
        return accumulativeDataFileCount;
    }

    public void setAccumulativeDataFileCount(Integer accumulativeDataFileCount) {
        this.accumulativeDataFileCount = accumulativeDataFileCount;
    }
}