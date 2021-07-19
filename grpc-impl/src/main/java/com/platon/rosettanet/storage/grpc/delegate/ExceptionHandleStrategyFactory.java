package com.platon.rosettanet.storage.grpc.delegate;

public class ExceptionHandleStrategyFactory {
    public static final BizExceptionHandleStrategy bizExceptionHandleStrategy = new BizExceptionHandleStrategy();
    public static final GenericExceptionHandlerStrategy genericExceptionHandlerStrategy = new GenericExceptionHandlerStrategy();

    public static ExceptionHandleStrategy getStrategy(Exception exception) {
        if (bizExceptionHandleStrategy.canHandle(exception)) {
            return bizExceptionHandleStrategy;
        }

        return genericExceptionHandlerStrategy;
    }
}
