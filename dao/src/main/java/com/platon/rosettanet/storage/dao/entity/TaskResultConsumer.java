package com.platon.rosettanet.storage.dao.entity;

public class TaskResultConsumer {
    private String taskId;

    private String consumerIdentityId;
    private String consumerPartyId;

    private String producerIdentityId;
    private String producerPartyId;

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

    public String getConsumerPartyId() {
        return consumerPartyId;
    }

    public void setConsumerPartyId(String consumerPartyId) {
        this.consumerPartyId = consumerPartyId;
    }

    public String getProducerIdentityId() {
        return producerIdentityId;
    }

    public void setProducerIdentityId(String producerIdentityId) {
        this.producerIdentityId = producerIdentityId;
    }

    public String getProducerPartyId() {
        return producerPartyId;
    }

    public void setProducerPartyId(String producerPartyId) {
        this.producerPartyId = producerPartyId;
    }
}