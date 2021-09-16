package com.platon.rosettanet.storage.dao.entity;

public class TaskMetaData {
    private String taskId;

    private String metaDataId;

    private String identityId;

    private String partyId;

    private Integer keyColumnIdx;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(String metaDataId) {
        this.metaDataId = metaDataId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public Integer getKeyColumnIdx() {
        return keyColumnIdx;
    }

    public void setKeyColumnIdx(Integer keyColumnIdx) {
        this.keyColumnIdx = keyColumnIdx;
    }
}
