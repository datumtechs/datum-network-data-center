package com.platon.metis.storage.dao.entity;

import java.time.LocalDateTime;

public class OrgInfo {
    private String identityId;

    private String identityType;

    private String nodeId;

    private String orgName;

    private Integer status;


    private Integer accumulativeDataFileCount;
    private LocalDateTime updateAt;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAccumulativeDataFileCount() {
        return accumulativeDataFileCount;
    }

    public void setAccumulativeDataFileCount(Integer accumulativeDataFileCount) {
        this.accumulativeDataFileCount = accumulativeDataFileCount;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}