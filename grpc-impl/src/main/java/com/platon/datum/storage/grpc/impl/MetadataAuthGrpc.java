package com.platon.datum.storage.grpc.impl;

import carrier.types.Common;
import carrier.types.Metadata;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.MetaDataAuth;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.MetaDataAuthService;
import com.platon.datum.storage.service.OrgInfoService;
import common.constant.CarrierEnum;
import datacenter.api.Auth;
import datacenter.api.MetadataAuthServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class MetadataAuthGrpc extends MetadataAuthServiceGrpc.MetadataAuthServiceImplBase {

    @Resource
    private OrgInfoService orgInfoService;

    @Resource
    private MetaDataAuthService metaDataAuthService;

    @Resource
    private ConvertorService convertorService;


    /**
     * <pre>
     * v2.0
     * 存储元数据鉴权申请记录
     * </pre>
     */
    @Override
    public void saveMetadataAuthority(Auth.MetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveMetadataAuthorityInternal(input),
                "saveMetadataAuthority");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void saveMetadataAuthorityInternal(Auth.MetadataAuthorityRequest request){
        MetaDataAuth metaDataAuth = convertMetadataAuthorityPB(request.getMetadataAuthority());
        metaDataAuth.setDataStatus(CarrierEnum.DataStatus.DataStatus_Valid_VALUE);
        metaDataAuthService.insertSelective(metaDataAuth);
    }


    private static MetaDataAuth convertMetadataAuthorityPB(Metadata.MetadataAuthorityPB metadataAuthorityPB) {
        MetaDataAuth metaDataAuth = new MetaDataAuth();
        metaDataAuth.setMetaDataAuthId(metadataAuthorityPB.getMetadataAuthId());
        metaDataAuth.setUser(metadataAuthorityPB.getUser());
        metaDataAuth.setDataId(metadataAuthorityPB.getDataId());
        metaDataAuth.setDataStatus(metadataAuthorityPB.getDataStatusValue());
        metaDataAuth.setUserType(metadataAuthorityPB.getUserTypeValue());
        metaDataAuth.setIdentityId(metadataAuthorityPB.getAuth().getOwner().getIdentityId());
        metaDataAuth.setMetaDataId(metadataAuthorityPB.getAuth().getMetadataId());
        metaDataAuth.setUsageType(metadataAuthorityPB.getAuth().getUsageRule().getUsageTypeValue());
        metaDataAuth.setAuditOption(metadataAuthorityPB.getAuditOptionValue());
        metaDataAuth.setAuditSuggestion(metadataAuthorityPB.getAuditSuggestion());
        metaDataAuth.setState(metadataAuthorityPB.getStateValue());
        metaDataAuth.setSign(Hex.encodeHexString(metadataAuthorityPB.getSign().toByteArray()));
        metaDataAuth.setPublishAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getPublishAt()));
        metaDataAuth.setUpdateAt(cn.hutool.core.date.LocalDateTimeUtil.ofUTC(Instant.now()));
        metaDataAuth.setNonce(metadataAuthorityPB.getNonce());
        if (metadataAuthorityPB.getApplyAt() > 0) {
            metaDataAuth.setApplyAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getApplyAt()));
        }
        if (metadataAuthorityPB.getAuditAt() > 0) {
            metaDataAuth.setAuditAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getAuditAt()));
        }
        if (metaDataAuth.getUsageType() == CarrierEnum.MetadataUsageType.Usage_Period.ordinal()) {
            metaDataAuth.setStartAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getAuth().getUsageRule().getStartAt()));
            metaDataAuth.setEndAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getAuth().getUsageRule().getEndAt()));
            metaDataAuth.setExpire(metadataAuthorityPB.getUsedQuo().getExpire() ? 1 : 0);
        } else if (metaDataAuth.getUsageType() == CarrierEnum.MetadataUsageType.Usage_Times.ordinal()) {
            metaDataAuth.setTimes(metadataAuthorityPB.getAuth().getUsageRule().getTimes());
            metaDataAuth.setUsedTimes(metadataAuthorityPB.getUsedQuo().getUsedTimes());
        }
        return metaDataAuth;
    }


    /**
     * <pre>
     * 数据授权审核，规则：
     * 1、授权后，可以将审核结果绑定到原有申请记录之上
     * </pre>
     */

    @Override
    public void updateMetadataAuthority(Auth.MetadataAuthorityRequest request,
                                        io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateMetadataAuthorityInternal(input),
                "updateMetadataAuthority");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void updateMetadataAuthorityInternal(Auth.MetadataAuthorityRequest request){
        MetaDataAuth metaDataAuth = convertMetadataAuthorityPB(request.getMetadataAuthority());
        metaDataAuthService.updateSelective(metaDataAuth);
    }

    /**
     * <pre>
     * 获取数据授权申请列表
     * 规则：参数存在时根据条件获取，参数不存在时全量返回
     * </pre>
     */
    @Override
    public void listMetadataAuthority(Auth.ListMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<Auth.ListMetadataAuthorityResponse> responseObserver) {
        Auth.ListMetadataAuthorityResponse  response = GrpcImplUtils.query(
                request,
                input -> listMetadataAuthorityInternal(input),
                bizOut -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadataAuthorities(bizOut)
                        .build(),
                bizError -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataAuthority"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    public List<Metadata.MetadataAuthorityPB> listMetadataAuthorityInternal(Auth.ListMetadataAuthorityRequest request){
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }
        String identityId = request.getIdentityId();
        List<MetaDataAuth> metaDataAuthList = metaDataAuthService.syncMetaDataAuth(identityId, lastUpdateAt, request.getPageSize());
        List<Metadata.MetadataAuthorityPB> metadataAuthorityPBList = metaDataAuthList.parallelStream().map(metaDataAuth -> this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth)).collect(Collectors.toList());
        return metadataAuthorityPBList;
    }

    /**
     * <pre>
     * v2.0
     * 查询元数据鉴权申请记录
     * </pre>
     */
    @Override
    public void findMetadataAuthorityById(Auth.FindMetadataAuthorityByIdRequest request,
                                      io.grpc.stub.StreamObserver<Auth.FindMetadataAuthorityByIdResponse> responseObserver) {
        Auth.FindMetadataAuthorityByIdResponse response = GrpcImplUtils.query(
                request,
                input -> getMetadataAuthorityById(input.getMetadataAuthId()),
                bizOut -> Auth.FindMetadataAuthorityByIdResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setMetadataAuthority(bizOut)
                        .build(),
                bizError -> Auth.FindMetadataAuthorityByIdResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Auth.FindMetadataAuthorityByIdResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataAuthorityById"
        );

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * FindMetadataAuthorityByIds retrieves data by ids.  add by v0.5.0
     * </pre>
     */
    @Override
    public void findMetadataAuthorityByIds(Auth.FindMetadataAuthorityByIdsRequest request,
                                          io.grpc.stub.StreamObserver<Auth.ListMetadataAuthorityResponse> responseObserver) {
        Auth.ListMetadataAuthorityResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataAuthorityByIdsInternal(input),
                bizOut -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadataAuthorities(bizOut)
                        .build(),
                bizError -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Auth.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataAuthorityByIds"
        );

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public List<Metadata.MetadataAuthorityPB> findMetadataAuthorityByIdsInternal(Auth.FindMetadataAuthorityByIdsRequest request){
        return request.getMetadataAuthIdsList().stream().map(metadataAuthId -> getMetadataAuthorityById(metadataAuthId)).collect(Collectors.toList());
    }

    public Metadata.MetadataAuthorityPB getMetadataAuthorityById(String metadataAuthId){
        MetaDataAuth metaDataAuth = metaDataAuthService.findByPK(metadataAuthId);
        if (metaDataAuth == null) {
            throw new BizException(CodeEnums.METADATA_AUTHORITY_NOT_FOUND);
        }
        Metadata.MetadataAuthorityPB metadataAuthorityPB = this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth);
        return metadataAuthorityPB;
    }
}
