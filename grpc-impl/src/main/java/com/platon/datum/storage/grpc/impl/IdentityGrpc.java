package com.platon.datum.storage.grpc.impl;

import carrier.types.Common;
import carrier.types.Identitydata;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.OrgInfoService;
import common.constant.CarrierEnum;
import datacenter.api.Auth;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class IdentityGrpc extends datacenter.api.IdentityServiceGrpc.IdentityServiceImplBase {

    @Resource
    private OrgInfoService orgInfoService;

    @Resource
    private ConvertorService convertorService;

    /**
     * <pre>
     * 拉去所有的身份数据
     * </pre>
     */
    @Override
    public void listIdentity(Auth.ListIdentityRequest request,
                             io.grpc.stub.StreamObserver<Auth.ListIdentityResponse> responseObserver) {
        Auth.ListIdentityResponse response = GrpcImplUtils.query(
                request,
                input -> listIdentityInternal(input),
                bizOut -> Auth.ListIdentityResponse.newBuilder()
                            .setStatus(CodeEnums.SUCCESS.getCode())
                            .setMsg(CodeEnums.SUCCESS.getMessage())
                            .addAllIdentities(bizOut).build(),
                bizError -> Auth.ListIdentityResponse.newBuilder()
                    .setStatus(bizError.getCode())
                    .setMsg(bizError.getMessage())
                    .build(),
                error -> Auth.ListIdentityResponse.newBuilder()
                    .setStatus(CodeEnums.EXCEPTION.getCode())
                    .setMsg(error.getMessage())
                    .build(),"listIdentity"
                );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Identitydata.IdentityPB> listIdentityInternal(Auth.ListIdentityRequest request){
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }
        List<OrgInfo> orgInfoList = orgInfoService.syncOrgInfo(lastUpdateAt, request.getPageSize());
        List<Identitydata.IdentityPB> organizationList = orgInfoList.parallelStream().map(orgInfo -> this.convertorService.toProtoIdentityPB(orgInfo)).collect(Collectors.toList());
        return organizationList;
    }

    /**
     * <pre>
     * 存储身份信息（节点用于申请接入网络的基本信息，详细的存于本地）
     * </pre>
     */

    @Override
    public void saveIdentity(Auth.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveIdentityInternal(input),
                "saveIdentity");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void saveIdentityInternal(Auth.SaveIdentityRequest request) {
        Identitydata.IdentityPB information = request.getInformation();
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

    @Override
    public void revokeIdentity(Auth.RevokeIdentityRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> revokeIdentityInternal(input),
                "revokeIdentityJoin");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Transactional
    public void revokeIdentityInternal(Auth.RevokeIdentityRequest request) {
        OrgInfo orgInfo = orgInfoService.findByPK(request.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", request.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        orgInfoService.updateStatus(request.getIdentityId(), CarrierEnum.CommonStatus.CommonStatus_Invalid.ordinal());
    }

    /**
     * 通过id查询身份信息
     */
    @Override
    public void findIdentity(Auth.FindIdentityRequest request,
                             io.grpc.stub.StreamObserver<Auth.FindIdentityResponse> responseObserver) {
        Auth.FindIdentityResponse response = GrpcImplUtils.query(
                request,
                input -> findIdentity(input),
                bizOut -> Auth.FindIdentityResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setIdentity(bizOut).build(),
                bizError -> Auth.FindIdentityResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Auth.FindIdentityResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listIdentity"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Identitydata.IdentityPB findIdentity(Auth.FindIdentityRequest request){
        OrgInfo orgInfo = orgInfoService.findByPK(request.getIdentityId());
        if(orgInfo == null){
            log.error("identity not found. identityId:={}", request.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        return convertorService.toProtoIdentityPB(orgInfo);
    }

    /**
     * <pre>
     * 更新指定组织的credential
     * </pre>
     */

    @Override
    public void updateIdentityCredential(Auth.UpdateIdentityCredentialRequest request,
                                         io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateIdentityCredentialInternal(input),
                "updateIdentityCredential");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void updateIdentityCredentialInternal(Auth.UpdateIdentityCredentialRequest request){
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
