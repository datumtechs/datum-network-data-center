package com.platon.datum.storage.common.enums;

import lombok.Getter;

@Getter
public enum CodeEnums {

    SUCCESS(0, "Success"),
    IDENTITY_VI_HAVE_SET(1000, "VC cannot be set repeatedly");

    private Integer code;
    private String message;

    CodeEnums(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
