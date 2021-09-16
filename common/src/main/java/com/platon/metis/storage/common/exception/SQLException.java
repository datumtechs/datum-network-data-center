package com.platon.metis.storage.common.exception;

public class SQLException extends BizException {
    private final static int CODE = 1000;
    private final static String MESSAGE = "SQL exception.";

    public SQLException() {
        super(CODE, MESSAGE);
    }

    public SQLException(String msg) {
        super(CODE, msg);
    }
}
