package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.MetaDataAuth;
import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.*;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.MetaDataAuthService;
import com.platon.rosettanet.storage.service.OrgInfoService;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class IdentityGrpc extends IdentityServiceGrpc.IdentityServiceImplBase {

    @Autowired
    private OrgInfoService orgInfoService;

    @Autowired
    private MetaDataAuthService metaDataAuthService;

    @Autowired
    private ConvertorService convertorService;

    /**
     * <pre>
     * 拉去所有的身份数据
     * </pre>
     */
    public void getIdentityList(IdentityListRequest request,
                                io.grpc.stub.StreamObserver<IdentityListResponse> responseObserver) {

        log.debug("getIdentityList, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<OrgInfo> orgInfoList = orgInfoService.syncOrgInfo(lastUpdateAt);
        List<Organization> organizationList = orgInfoList.parallelStream().map(orgInfo -> {
            return this.convertorService.toProtoOrganization(orgInfo);
        }).collect(Collectors.toList());

        IdentityListResponse response = IdentityListResponse.newBuilder().addAllIdentities(organizationList).build();

        log.debug("getIdentityList response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 存储身份信息（节点用于申请接入网络的基本信息，详细的存于本地）
     * </pre>
     */
    public void saveIdentity(com.platon.rosettanet.storage.grpc.lib.api.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("saveIdentity, request:{}", request);

        OrgInfo orgInfo = orgInfoService.findByPK(request.getMember().getIdentityId());

        if (orgInfo==null){
            orgInfo = new OrgInfo();
            orgInfo.setIdentityId(request.getMember().getIdentityId());
            orgInfo.setNodeId(request.getMember().getNodeId());
            orgInfo.setOrgName(request.getMember().getNodeName());
            orgInfo.setIdentityType(request.getCredential());
            orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());
            orgInfoService.insert(orgInfo);
        }else{

            orgInfo.setIdentityId(request.getMember().getIdentityId());
            orgInfo.setNodeId(request.getMember().getNodeId());
            orgInfo.setOrgName(request.getMember().getNodeName());
            orgInfo.setIdentityType(request.getCredential());
            orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());
            orgInfoService.update(orgInfo);
        }

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("saveIdentity response:{}, Status.code:{}", response, Status.OK.getCode().value());

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    /**
     * <pre>
     * 注销准入网络
     * </pre>
     */
    public void revokeIdentityJoin(com.platon.rosettanet.storage.grpc.lib.api.RevokeIdentityJoinRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("revokeIdentityJoin, request:{}", request);

        orgInfoService.updateStatus(request.getMember().getIdentityId(), CommonStatus.CommonStatus_NonNormal.ordinal());

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokeIdentityJoin response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * v2.0
     * 存储元数据鉴权申请记录
     * </pre>
     */
    public void saveMetadataAuthority(com.platon.rosettanet.storage.grpc.lib.api.SaveMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {
        log.debug("saveMetadataAuthority, request:{}", request);

        MetaDataAuth metaDataAuth = new MetaDataAuth();
        metaDataAuth.setMetaDataAuthId(request.getMetadataAuthId());
        metaDataAuth.setUserId(request.getUser());
        metaDataAuth.setUserType(request.getUserType().ordinal());
        metaDataAuth.setMetaDataId(request.getAuth().getMetadataId());
        metaDataAuth.setAuthType(request.getAuth().getUsage().getUsageType().ordinal());
        metaDataAuth.setUserIdentityId(request.getAuth().getOwner().getIdentityId());

        if(metaDataAuth.getAuthType() == MetadataUsageType.Usage_Period.ordinal()){
            metaDataAuth.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getAuth().getUsage().getEndAt()), ZoneOffset.UTC));
        } else if (metaDataAuth.getAuthType() == MetadataUsageType.Usage_Times.ordinal()){
            metaDataAuth.setTimes(request.getAuth().getUsage().getTimes());
        }
        metaDataAuth.setStatus(AuditMetadataOption.Audit_Pending.ordinal());

        metaDataAuthService.insert(metaDataAuth);

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("saveMetadataAuthority response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 数据授权审核，规则：
     * 1、授权后，可以将审核结果绑定到原有申请记录之上
     * </pre>
     */
    public void auditMetadataAuthority(com.platon.rosettanet.storage.grpc.lib.api.AuditMetadataAuthorityRequest request,
                                       io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("auditMetadataAuthority, request:{}", request);

        metaDataAuthService.updateStatus(request.getMetadataAuthId(), request.getAudit().ordinal());

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("auditMetadataAuthority response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 获取数据授权申请列表
     * 规则：参数存在时根据条件获取，参数不存在时全量返回
     * </pre>
     */
    public void getMetadataAuthorityList(com.platon.rosettanet.storage.grpc.lib.api.MetadataAuthorityListRequest request,
                                         io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.MetadataAuthorityListResponse> responseObserver) {

        log.debug("getMetadataAuthorityList, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);


        }
        String identityId = request.getIdentityId();

        List<MetaDataAuth> metaDataAuthList = metaDataAuthService.syncMetaDataAuth(identityId, lastUpdateAt);
        List<MetadataAuthorityDetail> metaDataAuthorityDetailList = metaDataAuthList.parallelStream().map(metaDataAuth -> {
            return this.convertorService.toProtoMetaDataAuthorityResponse(metaDataAuth);
        }).collect(Collectors.toList());

        MetadataAuthorityListResponse response = MetadataAuthorityListResponse.newBuilder().addAllAuthorities(metaDataAuthorityDetailList).build();

        log.debug("getMetadataAuthorityList response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
