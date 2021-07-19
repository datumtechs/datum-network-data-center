package com.platon.rosettanet.storage.grpc.interceptor;


import com.platon.rosettanet.storage.common.exception.BizException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;


@Slf4j
@GrpcAdvice
public class ExceptionGrpcInterceptor {
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleException(Exception ex) {

        log.error("Error, message {}", ex.getMessage());

        int code =  0;
        String message = "";
        if (ex instanceof BizException) {
            BizException bizEx = (BizException)ex;
            code = bizEx.getErrorCode();
            message = bizEx.getMessage();
        }else{
            code = Status.INTERNAL.getCode().value();
            message = ex.getMessage();
        }

        com.google.rpc.Status status =
                com.google.rpc.Status.newBuilder()
                        .setCode(code)
                        .setMessage(message)
                         .build();

        return StatusProto.toStatusRuntimeException(status);
    }
}
