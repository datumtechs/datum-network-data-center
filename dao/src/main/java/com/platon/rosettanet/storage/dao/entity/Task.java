package com.platon.rosettanet.storage.dao.entity;

import java.time.LocalDateTime;

public class Task {
    private String id;

    private String taskName;

    private Long requiredMemory;

    private Integer requiredCore;

    private Long requiredBandwidth;

    private Long requiredDuration;

    private String ownerIdentityId;

    private LocalDateTime createAt;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Long usedMemory;

    private Integer usedCore;

    private Long usedBandwidth;

    private Long usedFileSize;

    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getRequiredMemory() {
        return requiredMemory;
    }

    public void setRequiredMemory(Long requiredMemory) {
        this.requiredMemory = requiredMemory;
    }

    public Integer getRequiredCore() {
        return requiredCore;
    }

    public void setRequiredCore(Integer requiredCore) {
        this.requiredCore = requiredCore;
    }

    public Long getRequiredBandwidth() {
        return requiredBandwidth;
    }

    public void setRequiredBandwidth(Long requiredBandwidth) {
        this.requiredBandwidth = requiredBandwidth;
    }

    public Long getRequiredDuration() {
        return requiredDuration;
    }

    public void setRequiredDuration(Long requiredDuration) {
        this.requiredDuration = requiredDuration;
    }

    public String getOwnerIdentityId() {
        return ownerIdentityId;
    }

    public void setOwnerIdentityId(String ownerIdentityId) {
        this.ownerIdentityId = ownerIdentityId;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
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

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Integer getUsedCore() {
        return usedCore;
    }

    public void setUsedCore(Integer usedCore) {
        this.usedCore = usedCore;
    }

    public Long getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setUsedBandwidth(Long usedBandwidth) {
        this.usedBandwidth = usedBandwidth;
    }

    public Long getUsedFileSize() {
        return usedFileSize;
    }

    public void setUsedFileSize(Long usedFileSize) {
        this.usedFileSize = usedFileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}