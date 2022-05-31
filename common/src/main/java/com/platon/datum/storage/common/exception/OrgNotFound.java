package com.platon.datum.storage.common.exception;

import com.platon.datum.storage.common.exception.BizException;

public class OrgNotFound extends BizException {
    private final static int CODE = 1003;
    private final static String MESSAGE = "organization not found.";

    public OrgNotFound() {
        super(CODE, MESSAGE);
    }

    public OrgNotFound(String msg) {
        super(CODE, msg);
    }
}
