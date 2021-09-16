package com.platon.metis.storage.dao.entity;

public class ScheduleServer {
    private String id;

    private String identityId;

    private String internalIp;

    private String internalPort;

    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getInternalIp() {
        return internalIp;
    }

    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }

    public String getInternalPort() {
        return internalPort;
    }

    public void setInternalPort(String internalPort) {
        this.internalPort = internalPort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}