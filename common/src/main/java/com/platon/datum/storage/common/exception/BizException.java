package com.platon.datum.storage.common.exception;

import com.platon.datum.storage.common.enums.CodeEnums;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizException extends RuntimeException{

    private int code;
    private String message;

    public BizException(CodeEnums codeEnums) {
        super(codeEnums.getMessage());
        this.code = codeEnums.getCode();
        this.message = codeEnums.getMessage();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
