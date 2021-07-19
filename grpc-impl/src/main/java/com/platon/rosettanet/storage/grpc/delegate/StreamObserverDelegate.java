package com.platon.rosettanet.storage.grpc.delegate;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;


public class StreamObserverDelegate<ReqT extends Message, RespT extends Message> implements StreamObserver<RespT> {
    private static final Logger logger = LoggerFactory.getLogger(StreamObserverDelegate.class);

    private StreamObserver<RespT> originResponseObserver;

    private Boolean isCompleted;

    public StreamObserverDelegate(StreamObserver<RespT> originResponseObserver) {
        if (originResponseObserver == null) {
            logger.error("originResponseObserver must not null!");
        }
        this.originResponseObserver = originResponseObserver;
        this.isCompleted = false;
    }

    @Override
    public void onNext(RespT value) {
        if (!this.isCompleted && this.originResponseObserver != null) {
            this.originResponseObserver.onNext(value);
            this.isCompleted = true;
        }
    }

    @Override
    public void onError(Throwable t) {
        if (this.isCompleted) {
            return;
        }
        this.isCompleted = true;

        if (t instanceof Exception) {
            Exception exception = Exception.class.cast(t);
            ExceptionHandleStrategyFactory.getStrategy(exception).handleException(exception, this.originResponseObserver);
        } else {
            logger.info("gRPC Servant Error:{}", t.toString());
        }
    }

    @Override
    public void onCompleted() {
        if (!this.isCompleted && originResponseObserver != null) {
            originResponseObserver.onCompleted();
            this.isCompleted = true;
        }
    }

    /**
     * 执行业务(自动处理异常)
     *
     * @author masaiqi
     * @date 2021/4/12 18:11
     */
    public RespT executeWithException(Function<ReqT, RespT> function, ReqT request) {
        RespT response = null;
        try {
            response = function.apply(request);
        } catch (Throwable e) {
            this.onError(e);
        }
        return response;
    }

    /**
     * 执行业务(自动处理异常)
     *
     * @author masaiqi
     * @date 2021/4/12 18:11
     */
    public RespT executeWithException(Supplier<RespT> supplier) {
        RespT response = null;
        try {
            response = supplier.get();
        } catch (Throwable e) {
            this.onError(e);
        }
        return response;
    }

    /**
     * 执行业务(自动处理异常)
     *
     * @author masaiqi
     * @date 2021/4/12 18:11
     */
    public void executeWithException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            this.onError(e);
        }
    }
}

