package com.platon.rosettanet.storage.grpc.delegate;

import io.grpc.stub.StreamObserver;

public interface ExceptionHandleStrategy {
    /**
     * Whether the current exception can be handled
     *
     * @param exception exception instance.
     * @return true if current class can Handle exception.
     * @author <a href="mailto:masaiqi.com@gmail.com">masaiqi</a>
     * @date 2021-06-29 19:42
     */
    boolean canHandle(Exception exception);

    /**
     * Handle Exception
     *
     * @param exception exception instance.
     * @author <a href="mailto:masaiqi.com@gmail.com">masaiqi</a>
     * @date 2021-06-29 19:46
     */
    void handleException(Exception exception, StreamObserver streamObserver);
}
