package com.platon.metis.storage.common.exception;

public class TaskResultConsumerNotFound extends BizException {
    private final static int CODE = 1006;
    private final static String MESSAGE = "Task result consumer not found.";

    public TaskResultConsumerNotFound() {
        super(CODE, MESSAGE);
    }

    public TaskResultConsumerNotFound(String msg) {
        super(CODE, msg);
    }
}
