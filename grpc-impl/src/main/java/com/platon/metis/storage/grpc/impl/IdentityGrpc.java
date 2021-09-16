package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.dao.entity.MetaDataAuth;
import com.platon.metis.storage.dao.entity.OrgInfo;
import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.common.*;
import com.platon.metis.storage.grpc.lib.types.MetadataAuthorityPB;
import com.platon.metis.storage.service.ConvertorService;
import com.platon.metis.storage.service.MetaDataAuthService;
import com.platon.metis.storage.service.OrgInfoService;
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
    public void listIdentity(ListIdentityRequest request,
                                io.grpc.stub.StreamObserver<ListIdentityResponse> responseObserver) {

        log.debug("listIdentity, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<OrgInfo> orgInfoList = orgInfoService.syncOrgInfo(lastUpdateAt);
        List<Organization> organizationList = orgInfoList.parallelStream().map(orgInfo -> {
            return this.convertorService.toProtoOrganization(orgInfo);
        }).collect(Collectors.toList());

        ListIdentityResponse response = ListIdentityResponse.newBuilder().addAllIdentities(organizationList).build();

        log.debug("listIdentity response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 存储身份信息（节点用于申请接入网络的基本信息，详细的存于本地）
     * </pre>
     */
    public void saveIdentity(com.platon.metis.storage.grpc.lib.api.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {

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
    public void revokeIdentity(com.platon.metis.storage.grpc.lib.api.RevokeIdentityRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("revokeIdentityJoin, request:{}", request);

        orgInfoService.updateStatus(request.getIdentityId(), CommonStatus.CommonStatus_NonNormal.ordinal());

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
    public void saveMetadataAuthority(com.platon.metis.storage.grpc.lib.api.MetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {
        log.debug("saveMetadataAuthority, request:{}", request);

        MetaDataAuth metaDataAuth = new MetaDataAuth();
        metaDataAuth.setMetaDataAuthId(request.getMetadataAuthority().getMetadataAuthId());
        metaDataAuth.setUserId(request.getMetadataAuthority().getUser());
        metaDataAuth.setUserType(request.getMetadataAuthority().getUserType().ordinal());
        metaDataAuth.setMetaDataId(request.getMetadataAuthority().getAuth().getMetadataId());
        metaDataAuth.setAuthType(request.getMetadataAuthority().getAuth().getUsageRule().getUsageType().ordinal());
        metaDataAuth.setUserIdentityId(request.getMetadataAuthority().getAuth().getOwner().getIdentityId());

        if(metaDataAuth.getAuthType() == MetadataUsageType.Usage_Period.ordinal()){
            metaDataAuth.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getMetadataAuthority().getAuth().getUsageRule().getEndAt()), ZoneOffset.UTC));
        } else if (metaDataAuth.getAuthType() == MetadataUsageType.Usage_Times.ordinal()){
            metaDataAuth.setTimes(request.getMetadataAuthority().getAuth().getUsageRule().getTimes());
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
     * v2.0
     * 查询元数据鉴权申请记录
     * </pre>
     */
    public void findMetadataAuthority(com.platon.metis.storage.grpc.lib.api.FindMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.FindMetadataAuthorityResponse> responseObserver) {
        log.debug("findMetadataAuthority, request:{}", request);

        MetaDataAuth metaDataAuth = metaDataAuthService.findByPK(request.getMetadataAuthId());

        MetadataAuthorityPB metadataAuthorityPB = this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth);

        FindMetadataAuthorityResponse response = FindMetadataAuthorityResponse.newBuilder()
                .setMetadataAuthority(metadataAuthorityPB)
                .build();

        log.debug("findMetadataAuthority response:{}", response);

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
    public void updateMetadataAuthority(com.platon.metis.storage.grpc.lib.api.MetadataAuthorityRequest request,
                                       io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("updateMetadataAuthority, request:{}", request);

        metaDataAuthService.updateStatus(request.getMetadataAuthority().getMetadataAuthId(), request.getMetadataAuthority().getAuditOption().ordinal());

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("updateMetadataAuthority response:{}", response);

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
    public void listMetadataAuthority(com.platon.metis.storage.grpc.lib.api.ListMetadataAuthorityRequest request,
                                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListMetadataAuthorityResponse> responseObserver) {

        log.debug("listMetadataAuthority, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);


        }
        String identityId = request.getIdentityId();

        List<MetaDataAuth> metaDataAuthList = metaDataAuthService.syncMetaDataAuth(identityId, lastUpdateAt);
        List<MetadataAuthorityPB> metaDataAuthorityDetailList = metaDataAuthList.parallelStream().map(metaDataAuth -> {
            return this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth);
        }).collect(Collectors.toList());

        ListMetadataAuthorityResponse response = ListMetadataAuthorityResponse.newBuilder().addAllMetadataAuthorities(metaDataAuthorityDetailList).build();

        log.debug("listMetadataAuthority response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
