package com.platon.rosettanet.storage.common.exception;

public class TaskNotFound extends BizException {
    private final static int CODE = 1002;
    private final static String MESSAGE = "Task not found.";

    public TaskNotFound() {
        super(CODE, MESSAGE);
    }

    public TaskNotFound(String msg) {
        super(CODE, msg);
    }
}
