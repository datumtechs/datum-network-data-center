package com.platon.rosettanet.storage.common.exception;

public class TaskMetaDataNotFound extends BizException {
    private final static int CODE = 1004;
    private final static String MESSAGE = "Task metadata not found.";

    public TaskMetaDataNotFound() {
        super(CODE, MESSAGE);
    }

    public TaskMetaDataNotFound(String msg) {
        super(CODE, msg);
    }
}
