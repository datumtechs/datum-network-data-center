package com.platon.metis.storage.dao.entity;

import java.time.LocalDateTime;

public class DataFile {
    private String metaDataId;

    private String originId;

    private String identityId;

    private String fileName;

    private String filePath;

    private Integer fileType;

    private String resourceName;

    private String industry;

    private Long size;

    private Integer rows;

    private Integer columns;

    private Boolean published;

    private LocalDateTime publishedAt;

    private Boolean hasTitle;

    private String remarks;

    private Integer status;

    private LocalDateTime updateAt;

    private Integer dfsDataStatus;
    private String dfsDataId;

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getHasTitle() {
        return hasTitle;
    }

    public void setHasTitle(Boolean hasTitle) {
        this.hasTitle = hasTitle;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(String metaDataId) {
        this.metaDataId = metaDataId;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
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
}