package com.platon.rosettanet.storage.grpc.interceptor;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {
    @GrpcGlobalServerInterceptor
    LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }

    @GrpcGlobalServerInterceptor
    ExceptionInterceptor exceptionInterceptor() {
        return new ExceptionInterceptor();
    }
}
