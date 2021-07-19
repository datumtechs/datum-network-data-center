package com.platon.rosettanet.storage.grpc.interceptor;

import com.platon.rosettanet.storage.common.exception.BizException;
import com.platon.rosettanet.storage.grpc.lib.ErrorMessage;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;


public class ExceptionInterceptor implements ServerInterceptor {

    public static final Metadata.Key<ErrorMessage> ERROR_MESSAGE_TRAILER_KEY =  ProtoUtils.keyForProto(ErrorMessage.getDefaultInstance());


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,ServerCallHandler<ReqT, RespT> serverCallHandler) {
        ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(serverCall, metadata);
        return new ExceptionHandlingServerCallListener<>(listener, serverCall, metadata);
    }

    private class ExceptionHandlingServerCallListener<ReqT, RespT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
        private ServerCall<ReqT, RespT> serverCall;
        private Metadata metadata;

        ExceptionHandlingServerCallListener(ServerCall.Listener<ReqT> listener, ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
            super(listener);
            this.serverCall = serverCall;
            this.metadata = metadata;
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (RuntimeException ex) {
                ErrorMessage errorMessage = null;
                if (ex instanceof BizException) {
                    BizException bizEx = (BizException)ex;
                    errorMessage = ErrorMessage.newBuilder()
                            .setCode(bizEx.getErrorCode())
                            .setMessage(bizEx.getMessage())
                            .build();
                }else{
                    errorMessage = ErrorMessage.newBuilder()
                            .setCode(Status.INTERNAL.getCode().value())
                            .setMessage(ex.getMessage())
                            .build();
                }
                metadata.put(ERROR_MESSAGE_TRAILER_KEY, errorMessage);
                handleException(ex, serverCall, metadata);
                throw ex;
            }
        }

        @Override
        public void onReady() {
            try {
                super.onReady();
            } catch (RuntimeException ex) {
                handleException(ex, serverCall, metadata);
                throw ex;
            }
        }

        private void handleException(RuntimeException exception, ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
            if (exception instanceof BizException) {
                serverCall.close(Status.INTERNAL.withDescription(exception.getMessage()), metadata);
            } else {
                serverCall.close(Status.UNKNOWN, metadata);
            }
        }
    }


/*    public Metadata fromThrowable(Throwable t){
        BizException bizException = (BizException) t;
        Metadata trailers = new Metadata();
        com.platon.rosettanet.storage.grpc.lib.ErrorInfo errorInfo = com.platon.rosettanet.storage.grpc.lib.ErrorInfo.newBuilder()
                .setErrorCode(String.valueOf(bizException.getErrorCode()) )
                .setDefaultMsg(bizException.getMessage())
                .build();
        Metadata.Key<com.platon.rosettanet.storage.grpc.lib.ErrorInfo> ERROR_INFO_TRAILER_KEY =
                ProtoUtils.keyForProto(errorInfo);
        trailers.put(ERROR_INFO_TRAILER_KEY, errorInfo);
        return trailers;
    }*/
}

