package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class TaskEvent {
    private Long id;

    private String taskId;

    private String eventType;

    private String identityId;
    private String partyId;

    private LocalDateTime eventAt;

    private String eventContent;
}