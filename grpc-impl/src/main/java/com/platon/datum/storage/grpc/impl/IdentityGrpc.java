package com.platon.datum.storage.grpc.impl;

import com.google.protobuf.Int32Value;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.MetaDataAuth;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.Metadata;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Identity;
import com.platon.datum.storage.grpc.datacenter.api.IdentityServiceGrpc;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.MetaDataAuthService;
import com.platon.datum.storage.service.OrgInfoService;
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
public class IdentityGrpc extends IdentityServiceGrpc.IdentityServiceImplBase {

    @Resource
    private OrgInfoService orgInfoService;

    @Resource
    private MetaDataAuthService metaDataAuthService;

    @Resource
    private ConvertorService convertorService;

    /**
     * <pre>
     * 拉去所有的身份数据
     * </pre>
     */
    @Override
    public void listIdentity(Identity.ListIdentityRequest request,
                             io.grpc.stub.StreamObserver<Identity.ListIdentityResponse> responseObserver) {
        Identity.ListIdentityResponse response = GrpcImplUtils.query(
                request,
                input -> listIdentityInternal(input),
                bizOut -> Identity.ListIdentityResponse.newBuilder()
//                            .setStatus(CodeEnums.SUCCESS.getCode())
                            .setMsg(CodeEnums.SUCCESS.getMessage())
                            .addAllIdentities(bizOut).build(),
                bizError -> Identity.ListIdentityResponse.newBuilder()
                    .setStatus(bizError.getCode())
                    .setMsg(bizError.getMessage())
                    .build(),
                error -> Identity.ListIdentityResponse.newBuilder()
                    .setStatus(CodeEnums.EXCEPTION.getCode())
                    .setMsg(error.getMessage())
                    .build(),"listIdentity"
                );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<IdentityData.IdentityPB> listIdentityInternal(Identity.ListIdentityRequest request){
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }
        List<OrgInfo> orgInfoList = orgInfoService.syncOrgInfo(lastUpdateAt, request.getPageSize());
        List<IdentityData.IdentityPB> organizationList = orgInfoList.parallelStream().map(orgInfo -> this.convertorService.toProtoIdentityPB(orgInfo)).collect(Collectors.toList());
        return organizationList;
    }

    /**
     * <pre>
     * 存储身份信息（节点用于申请接入网络的基本信息，详细的存于本地）
     * </pre>
     */
    @Transactional
    @Override
    public void saveIdentity(Identity.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveIdentityInternal(input),
                "saveIdentity");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void saveIdentityInternal(Identity.SaveIdentityRequest request) {
        IdentityData.IdentityPB information = request.getInformation();
        OrgInfo orgInfo = orgInfoService.findByPK(information.getIdentityId());

        if (orgInfo == null) {
            orgInfo = new OrgInfo();
            orgInfo.setIdentityType(information.getIdentityTypeValue());
            orgInfo.setIdentityId(information.getIdentityId());
            orgInfo.setNodeId(information.getNodeId());
            orgInfo.setNodeName(information.getNodeName());
            orgInfo.setDataId(information.getDataId());
            orgInfo.setDataStatus(CarrierEnum.DataStatus.DataStatus_Valid_VALUE);
            orgInfo.setStatus(CarrierEnum.CommonStatus.CommonStatus_Valid_VALUE);
            orgInfo.setCredential(information.getCredential());
            orgInfo.setImageUrl(information.getImageUrl());
            orgInfo.setDetails(information.getDetails());
            orgInfo.setNonce(information.getNonce());
            orgInfoService.insert(orgInfo);
        } else {
            orgInfo.setIdentityType(information.getIdentityTypeValue());
            orgInfo.setIdentityId(information.getIdentityId());
            orgInfo.setNodeId(information.getNodeId());
            orgInfo.setNodeName(information.getNodeName());
            orgInfo.setDataId(information.getDataId());
            orgInfo.setDataStatus(CarrierEnum.DataStatus.DataStatus_Valid_VALUE);
            orgInfo.setStatus(CarrierEnum.CommonStatus.CommonStatus_Valid_VALUE);
            orgInfo.setCredential(information.getCredential());
            orgInfo.setImageUrl(information.getImageUrl());
            orgInfo.setDetails(information.getDetails());
            orgInfo.setNonce(information.getNonce());
            orgInfoService.update(orgInfo);
        }
    }

    /**
     * <pre>
     * 注销准入网络
     * </pre>
     */
    @Transactional
    @Override
    public void revokeIdentity(Identity.RevokeIdentityRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> revokeIdentityInternal(input),
                "revokeIdentityJoin");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void revokeIdentityInternal(Identity.RevokeIdentityRequest request) {
        OrgInfo orgInfo = orgInfoService.findByPK(request.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", request.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        orgInfoService.updateStatus(request.getIdentityId(), CarrierEnum.CommonStatus.CommonStatus_Invalid.ordinal());
    }


    /**
     * <pre>
     * v2.0
     * 存储元数据鉴权申请记录
     * </pre>
     */
    @Transactional
    @Override
    public void saveMetadataAuthority(Identity.MetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveMetadataAuthorityInternal(input),
                "saveMetadataAuthority");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void saveMetadataAuthorityInternal(Identity.MetadataAuthorityRequest request){
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
     * v2.0
     * 查询元数据鉴权申请记录
     * </pre>
     */
    @Override
    public void findMetadataAuthority(Identity.FindMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<Identity.FindMetadataAuthorityResponse> responseObserver) {
        Identity.FindMetadataAuthorityResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataAuthorityInternal(input),
                bizOut -> Identity.FindMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setMetadataAuthority(bizOut)
                        .build(),
                bizError -> Identity.FindMetadataAuthorityResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Identity.FindMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataAuthority"
        );

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public Metadata.MetadataAuthorityPB findMetadataAuthorityInternal(Identity.FindMetadataAuthorityRequest request){
        MetaDataAuth metaDataAuth = metaDataAuthService.findByPK(request.getMetadataAuthId());
        if (metaDataAuth == null) {
            throw new BizException(CodeEnums.METADATA_AUTHORITY_NOT_FOUND);
        }
        Metadata.MetadataAuthorityPB metadataAuthorityPB = this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth);
        return metadataAuthorityPB;
    }

    /**
     * <pre>
     * 数据授权审核，规则：
     * 1、授权后，可以将审核结果绑定到原有申请记录之上
     * </pre>
     */
    @Transactional
    @Override
    public void updateMetadataAuthority(Identity.MetadataAuthorityRequest request,
                                        io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateMetadataAuthorityInternal(input),
                "updateMetadataAuthority");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateMetadataAuthorityInternal(Identity.MetadataAuthorityRequest request){
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
    public void listMetadataAuthority(Identity.ListMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<Identity.ListMetadataAuthorityResponse> responseObserver) {
        Identity.ListMetadataAuthorityResponse  response = GrpcImplUtils.query(
                request,
                input -> listMetadataAuthorityInternal(input),
                bizOut -> Identity.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadataAuthorities(bizOut)
                        .build(),
                bizError -> Identity.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Identity.ListMetadataAuthorityResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataAuthority"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    public List<Metadata.MetadataAuthorityPB> listMetadataAuthorityInternal(Identity.ListMetadataAuthorityRequest request){
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
     * 更新指定组织的credential
     * </pre>
     */
    @Transactional
    @Override
    public void updateIdentityCredential(Identity.UpdateIdentityCredentialRequest request,
                                        io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateIdentityCredentialInternal(input),
                "updateIdentityCredential");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateIdentityCredentialInternal(Identity.UpdateIdentityCredentialRequest request){
        OrgInfo orgInfo = orgInfoService.findByPK(request.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", request.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        if(!orgInfoService.updateCredential(request.getIdentityId(), request.getCredential())){
            throw new BizException(CodeEnums.ORG_VI_HAVE_SET);
        }
    }
}
