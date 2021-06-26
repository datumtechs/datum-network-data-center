package com.platon.rosettanet.storage.dao.entity;

public class TaskResultReceiver {
    private String taskId;

    private String receiverIdentityId;

    private String senderIdentityId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReceiverIdentityId() {
        return receiverIdentityId;
    }

    public void setReceiverIdentityId(String receiverIdentityId) {
        this.receiverIdentityId = receiverIdentityId;
    }

    public String getSenderIdentityId() {
        return senderIdentityId;
    }

    public void setSenderIdentityId(String senderIdentityId) {
        this.senderIdentityId = senderIdentityId;
    }
}