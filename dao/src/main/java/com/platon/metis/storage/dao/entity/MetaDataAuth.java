package com.platon.metis.storage.dao.entity;

import java.time.LocalDateTime;

public class MetaDataAuth {
    private String metaDataAuthId;

    private String userIdentityId;

    private String userId;

    private Integer userType;

    private String metaDataId;

    private Integer dfsDataStatus;

    private String dfsDataId;

    private Integer authType;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer times;

    private Boolean expired;

    private Integer usedTimes;

    private Integer status;

    private LocalDateTime applyAt;

    private String auditDesc;

    private LocalDateTime auditAt;

    private Integer authStatus;

    private LocalDateTime updateAt;

    private String authSign;

    public String getMetaDataAuthId() {
        return metaDataAuthId;
    }

    public void setMetaDataAuthId(String metaDataAuthId) {
        this.metaDataAuthId = metaDataAuthId;
    }

    public String getUserIdentityId() {
        return userIdentityId;
    }

    public void setUserIdentityId(String userIdentityId) {
        this.userIdentityId = userIdentityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(String metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Integer getDfsDataStatus() {
        return dfsDataStatus;
    }

    public void setDfsDataStatus(Integer dfsDataStatus) {
        this.dfsDataStatus = dfsDataStatus;
    }

    public String getDfsDataId() {
        return dfsDataId;
    }

    public void setDfsDataId(String dfsDataId) {
        this.dfsDataId = dfsDataId;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Integer getUsedTimes() {
        return usedTimes;
    }

    public void setUsedTimes(Integer usedTimes) {
        this.usedTimes = usedTimes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getApplyAt() {
        return applyAt;
    }

    public void setApplyAt(LocalDateTime applyAt) {
        this.applyAt = applyAt;
    }

    public String getAuditDesc() {
        return auditDesc;
    }

    public void setAuditDesc(String auditDesc) {
        this.auditDesc = auditDesc;
    }

    public LocalDateTime getAuditAt() {
        return auditAt;
    }

    public void setAuditAt(LocalDateTime auditAt) {
        this.auditAt = auditAt;
    }

    public Integer getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(Integer authStatus) {
        this.authStatus = authStatus;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public String getAuthSign() {
        return authSign;
    }

    public void setAuthSign(String authSign) {
        this.authSign = authSign;
    }
}