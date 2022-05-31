package com.platon.datum.storage.common.exception;

import com.platon.datum.storage.common.exception.BizException;

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
