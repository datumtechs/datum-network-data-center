package com.platon.rosettanet.storage.grpc.delegate;

import com.platon.rosettanet.storage.common.exception.BizException;
import com.platon.rosettanet.storage.grpc.lib.ErrorMessage;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

import java.util.Optional;

public class BizExceptionHandleStrategy implements ExceptionHandleStrategy{

    private final Metadata.Key<ErrorMessage> ERROR_INFO_TRAILER_KEY =
            ProtoUtils.keyForProto(ErrorMessage.getDefaultInstance());

    @Override
    public boolean canHandle(Exception exception) {
        return exception instanceof BizException;
    }

    @Override
    public void handleException(Exception exception, StreamObserver streamObserver) {
        // 业务异常，返回错误码和默认文案到客户端
        BizException bizException = (BizException) exception;
        Metadata trailers = new Metadata();
        ErrorMessage errorMessage = ErrorMessage.newBuilder()
                .setCode(bizException.getErrorCode())
                .setMessage(Optional.ofNullable(bizException.getMessage()).orElse(""))
                .build();
        trailers.put(ERROR_INFO_TRAILER_KEY, errorMessage);
        streamObserver.onError(Status.UNKNOWN
                .withCause(bizException)
                .asRuntimeException(trailers));
        // 抛出异常让当前业务感知
        throw bizException;
    }
}
