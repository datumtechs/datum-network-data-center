package com.platon.datum.storage.grpc.utils;

import carrier.types.Common;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class GrpcImplUtils {

    public static <I> Common.SimpleResponse saveOfUpdate(I input, Consumer<I> bizProcessor, String interfaceName){
        log.info(interfaceName + " request:{}", input);
        Common.SimpleResponse response;
        try {
            bizProcessor.accept(input);
            response = Common.SimpleResponse.newBuilder()
                    .setStatus(CodeEnums.SUCCESS.getCode())
                    .setMsg(CodeEnums.SUCCESS.getMessage())
                    .build();
        } catch (BizException e) {
            log.error(interfaceName + " error", e);
            response = Common.SimpleResponse.newBuilder()
                    .setStatus(e.getCode())
                    .setMsg(e.getMessage())
                    .build();
        } catch (Exception e){
            log.error(interfaceName + " error", e);
            response = Common.SimpleResponse.newBuilder()
                    .setStatus(CodeEnums.EXCEPTION.getCode())
                    .setMsg(e.getMessage())
                    .build();
        }
        log.info(interfaceName + " response:{}", response);
        return response;
    }

    public static <I,O,O1> O query(I input, Function<I, O1> bizProcessor,  Function<O1, O> onSuccess,  Function<BizException, O> onBizError,  Function<Exception, O> onError,String interfaceName){
        log.info(interfaceName + " request:{}", input);
        O response;
        try {
            O1 o1 = bizProcessor.apply(input);
            response = onSuccess.apply(o1);
        } catch (BizException e) {
            log.error(interfaceName + " error", e);
            response = onBizError.apply(e);
        } catch (Exception e){
            log.error(interfaceName + " error", e);
            response = onError.apply(e);
        }
        log.info(interfaceName + " response:{}", response);
        return response;
    }
}
