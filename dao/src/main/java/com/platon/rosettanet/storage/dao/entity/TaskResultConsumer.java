package com.platon.rosettanet.storage.dao.entity;

public class TaskResultConsumer {
    private String taskId;

    private String consumerIdentityId;

    private String producerIdentityId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getConsumerIdentityId() {
        return consumerIdentityId;
    }

    public void setConsumerIdentityId(String consumerIdentityId) {
        this.consumerIdentityId = consumerIdentityId;
    }

    public String getProducerIdentityId() {
        return producerIdentityId;
    }

    public void setProducerIdentityId(String producerIdentityId) {
        this.producerIdentityId = producerIdentityId;
    }
}