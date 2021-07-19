package com.platon.rosettanet.storage.common.exception;

public class BizException extends RuntimeException{
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public BizException(int errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }

    public BizException(int errorCode) {
        this.errorCode = errorCode;
    }

    public BizException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BizException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public BizException(int errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public BizException() {
    }

}
