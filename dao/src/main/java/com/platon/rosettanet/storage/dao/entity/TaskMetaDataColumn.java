package com.platon.rosettanet.storage.dao.entity;

public class TaskMetaDataColumn {
    private String taskId;

    private String metaDataId;

    private Integer selectedColumnIdx;

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

    public Integer getSelectedColumnIdx() {
        return selectedColumnIdx;
    }

    public void setSelectedColumnIdx(Integer selectedColumnIdx) {
        this.selectedColumnIdx = selectedColumnIdx;
    }
}