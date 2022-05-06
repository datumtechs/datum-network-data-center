package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.common.exception.BizException;
import com.platon.metis.storage.common.util.LocalDateTimeUtil;
import com.platon.metis.storage.dao.entity.MetaDataAuth;
import com.platon.metis.storage.dao.entity.OrgInfo;
import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.types.Base;
import com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse;
import com.platon.metis.storage.grpc.lib.types.IdentityPB;
import com.platon.metis.storage.grpc.lib.types.MetadataAuthorityPB;
import com.platon.metis.storage.service.ConvertorService;
import com.platon.metis.storage.service.MetaDataAuthService;
import com.platon.metis.storage.service.OrgInfoService;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
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
    @Override
    public void listIdentity(ListIdentityRequest request,
                             io.grpc.stub.StreamObserver<ListIdentityResponse> responseObserver) {

        log.debug("listIdentity, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<OrgInfo> orgInfoList = orgInfoService.syncOrgInfo(lastUpdateAt, request.getPageSize());
        List<IdentityPB> organizationList = orgInfoList.parallelStream().map(orgInfo -> {
            return this.convertorService.toProtoIdentityPB(orgInfo);
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
    @Transactional
    @Override
    public void saveIdentity(com.platon.metis.storage.grpc.lib.api.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("saveIdentity, request:{}", request);

        IdentityPB information = request.getInformation();
        OrgInfo orgInfo = orgInfoService.findByPK(information.getIdentityId());

        if (orgInfo == null) {
            orgInfo = new OrgInfo();
            orgInfo.setIdentityId(information.getIdentityId());
            orgInfo.setNodeId(information.getNodeId());
            orgInfo.setNodeName(information.getNodeName());
            orgInfo.setDataId(information.getDataId());
            orgInfo.setDataStatus(Base.DataStatus.DataStatus_Valid_VALUE);
            orgInfo.setStatus(Base.CommonStatus.CommonStatus_Valid_VALUE);
            orgInfo.setCredential(information.getCredential());
            orgInfo.setImageUrl(information.getImageUrl());
            orgInfo.setDetails(information.getDetails());
            orgInfo.setNonce(information.getNonce());
            orgInfoService.insert(orgInfo);
        } else {
            orgInfo.setIdentityId(information.getIdentityId());
            orgInfo.setNodeId(information.getNodeId());
            orgInfo.setNodeName(information.getNodeName());
            orgInfo.setDataId(information.getDataId());
            orgInfo.setDataStatus(Base.DataStatus.DataStatus_Valid_VALUE);
            orgInfo.setStatus(Base.CommonStatus.CommonStatus_Valid_VALUE);
            orgInfo.setCredential(information.getCredential());
            orgInfo.setImageUrl(information.getImageUrl());
            orgInfo.setDetails(information.getDetails());
            orgInfo.setNonce(information.getNonce());
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
    @Transactional
    @Override
    public void revokeIdentity(com.platon.metis.storage.grpc.lib.api.RevokeIdentityRequest request,
                               io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("revokeIdentityJoin, request:{}", request);

        orgInfoService.updateStatus(request.getIdentityId(), Base.CommonStatus.CommonStatus_Invalid.ordinal());

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
    @Transactional
    @Override
    public void saveMetadataAuthority(com.platon.metis.storage.grpc.lib.api.MetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {
        log.debug("saveMetadataAuthority, request:{}", request);

        MetaDataAuth metaDataAuth = convertMetadataAuthorityPB(request.getMetadataAuthority());

        metaDataAuth.setDataStatus(Base.DataStatus.DataStatus_Valid_VALUE);
        // metaDataAuth.setStatus(AuditMetadataOption.Audit_Pending.ordinal());

        metaDataAuthService.insertSelective(metaDataAuth);

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("saveMetadataAuthority response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static MetaDataAuth convertMetadataAuthorityPB(MetadataAuthorityPB metadataAuthorityPB) {
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
        if (metaDataAuth.getUsageType() == Base.MetadataUsageType.Usage_Period.ordinal()) {
            metaDataAuth.setStartAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getAuth().getUsageRule().getStartAt()));
            metaDataAuth.setEndAt(LocalDateTimeUtil.getLocalDateTme(metadataAuthorityPB.getAuth().getUsageRule().getEndAt()));
            metaDataAuth.setExpire(metadataAuthorityPB.getUsedQuo().getExpire() ? 1 : 0);
        } else if (metaDataAuth.getUsageType() == Base.MetadataUsageType.Usage_Times.ordinal()) {
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
    public void findMetadataAuthority(com.platon.metis.storage.grpc.lib.api.FindMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.FindMetadataAuthorityResponse> responseObserver) {
        log.debug("findMetadataAuthority, request:{}", request);

        MetaDataAuth metaDataAuth = metaDataAuthService.findByPK(request.getMetadataAuthId());
        if (metaDataAuth == null) {
            throw new BizException(-1, "metadata authority not found");
        }
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
    @Transactional
    @Override
    public void updateMetadataAuthority(com.platon.metis.storage.grpc.lib.api.MetadataAuthorityRequest request,
                                        io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("updateMetadataAuthority, request:{}", request);
        MetaDataAuth metaDataAuth = convertMetadataAuthorityPB(request.getMetadataAuthority());

        metaDataAuthService.updateSelective(metaDataAuth);

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
    @Override
    public void listMetadataAuthority(com.platon.metis.storage.grpc.lib.api.ListMetadataAuthorityRequest request,
                                      io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListMetadataAuthorityResponse> responseObserver) {

        log.debug("listMetadataAuthority, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }
        String identityId = request.getIdentityId();
        List<MetaDataAuth> metaDataAuthList = metaDataAuthService.syncMetaDataAuth(identityId, lastUpdateAt, request.getPageSize());


        ListMetadataAuthorityResponse response;
        if (CollectionUtils.isEmpty(metaDataAuthList)) {
            response = ListMetadataAuthorityResponse.newBuilder().build();

        } else {
            List<MetadataAuthorityPB> metaDataAuthorityDetailList = metaDataAuthList.parallelStream().map(metaDataAuth -> {
                return this.convertorService.toProtoMetadataAuthorityPB(metaDataAuth);
            }).collect(Collectors.toList());

            response = ListMetadataAuthorityResponse.newBuilder().addAllMetadataAuthorities(metaDataAuthorityDetailList).build();
        }
        log.debug("listMetadataAuthority response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
