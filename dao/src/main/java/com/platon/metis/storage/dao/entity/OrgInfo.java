package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class OrgInfo {
    private String identityId;

    private String identityType;

    private String nodeId;

    private String orgName;
    private String imageUrl;
    private String profile;

    private Integer status;


    private Integer accumulativeDataFileCount;
    private LocalDateTime updateAt;
}