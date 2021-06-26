package com.platon.rosettanet.storage.dao.entity;

public class TaskMetaDataColumn {
    private String taskId;

    private String metaDataId;

    private Integer columnIdx;

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

    public Integer getColumnIdx() {
        return columnIdx;
    }

    public void setColumnIdx(Integer columnIdx) {
        this.columnIdx = columnIdx;
    }
}