package com.platon.datum.storage.common.enums;

import lombok.Getter;

@Getter
public enum CodeEnums {
    // 通用错误
    SUCCESS(0, "Success"),
    EXCEPTION(1, "Internal System Error"),
    SQL_EXCEPTION(2, "数据库操作异常"),
    // 组织相关
    ORG_VI_HAVE_SET(1000, "VC cannot be set repeatedly"),
    ORG_NOT_FOUND(1001, "Organization does not exist"),
    // 元数据相关
    METADATA_NOT_FOUND(2000, "MetaData not found"),
    METADATA_AUTHORITY_NOT_FOUND(2001 , "MetaData authority not found"),
    METADATA_CONTRACT_HAVE_SET(2002 , "MetaData The voucher contract has been set up"),
    // 任务相关
    TASK_METADATA_NOT_FOUND(3000, "Task metadata not found"),
    ;

    private Integer code;
    private String message;

    CodeEnums(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
