package com.platon.metis.storage.common.exception;

public class MetaDataNotFound extends BizException {
    private final static int CODE = 1007;
    private final static String MESSAGE = "MetaData not found.";

    public MetaDataNotFound() {
        super(CODE, MESSAGE);
    }

    public MetaDataNotFound(String msg) {
        super(CODE, msg);
    }
}