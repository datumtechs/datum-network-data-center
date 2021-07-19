package com.platon.rosettanet.storage.grpc.delegate;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GenericExceptionHandlerStrategy implements ExceptionHandleStrategy{
    @Override
    public boolean canHandle(Exception exception) {
        return false;
    }

    @Override
    public void handleException(Exception exception, StreamObserver streamObserver) {
        streamObserver.onError(Status.UNKNOWN
                .withDescription(exception.getMessage())
                .withCause(exception)
                .asRuntimeException());
        // 抛出异常让上层业务感知(比如事务回滚等可能要用到)
        RuntimeException runtimeException = new RuntimeException(exception);
        runtimeException.setStackTrace(exception.getStackTrace());
        throw runtimeException;
    }
}
