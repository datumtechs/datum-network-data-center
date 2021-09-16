package com.platon.metis.storage.common.exception;

public class TaskPowerNotFound extends BizException {
    private final static int CODE = 1005;
    private final static String MESSAGE = "Task power not found.";

    public TaskPowerNotFound() {
        super(CODE, MESSAGE);
    }

    public TaskPowerNotFound(String msg) {
        super(CODE, msg);
    }
}
