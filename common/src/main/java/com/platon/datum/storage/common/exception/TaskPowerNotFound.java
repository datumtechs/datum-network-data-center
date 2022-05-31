package com.platon.datum.storage.common.exception;

import com.platon.datum.storage.common.exception.BizException;

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
