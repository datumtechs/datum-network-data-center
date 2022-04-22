package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
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

    private Integer auditOption;

    private LocalDateTime applyAt;

    private String auditDesc;

    private LocalDateTime auditAt;

    private Integer authStatus;

    private LocalDateTime updateAt;

    private String authSign;

}