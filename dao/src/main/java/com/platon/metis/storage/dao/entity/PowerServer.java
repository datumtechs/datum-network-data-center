package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class PowerServer extends BaseDomain{
    private String id;

    private String identityId;

    private Long memory;

    private Integer core;

    private Long bandwidth;

    private Long usedMemory;

    private Integer usedCore;

    private Long usedBandwidth;

    private Boolean published;

    private LocalDateTime publishedAt;

    private Integer status;

    private LocalDateTime updateAt;

}