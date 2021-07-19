package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.grpc.lib.IdentityListResponse;
import com.platon.rosettanet.storage.grpc.lib.IdentityServiceGrpc;
import com.platon.rosettanet.storage.grpc.lib.Organization;
import com.platon.rosettanet.storage.grpc.lib.SimpleResponse;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.OrgInfoService;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class IdentityGrpc extends IdentityServiceGrpc.IdentityServiceImplBase {

    @Autowired
    private OrgInfoService orgInfoService;

    @Autowired
    private ConvertorService convertorService;

    /**
     * <pre>
     * 拉去所有的身份数据
     * </pre>
     */
    public void getIdentityList(com.platon.rosettanet.storage.grpc.lib.IdentityListRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.IdentityListResponse> responseObserver) {

        log.debug("getIdentityList, request:{}", request);

        List<OrgInfo> orgInfoList = orgInfoService.listOrgInfo();
        List<Organization> organizationList = orgInfoList.stream().map(orgInfo -> {
            return this.convertorService.toProtoOrganization(orgInfo);
        }).collect(Collectors.toList());

        IdentityListResponse response = IdentityListResponse.newBuilder().addAllIdentityList(organizationList).build();

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
    public void saveIdentity(com.platon.rosettanet.storage.grpc.lib.SaveIdentityRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.SimpleResponse> responseObserver) {

        log.debug("saveIdentity, request:{}", request);
        //StreamObserverDelegate streamObserverDelegate = new StreamObserverDelegate(responseObserver);

        /*streamObserverDelegate.executeWithException(() -> {
            //
        });*/


        OrgInfo orgInfo = new OrgInfo();
        orgInfo.setIdentityId(request.getMember().getIdentityId());
        orgInfo.setNodeId(request.getMember().getNodeId());
        orgInfo.setOrgName(request.getMember().getName());
        orgInfo.setIdentityType(request.getCredential());
        orgInfo.setStatus("enabled");
        orgInfoService.insert(orgInfo);

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
    public void revokeIdentityJoin(com.platon.rosettanet.storage.grpc.lib.RevokeIdentityJoinRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.SimpleResponse> responseObserver) {

        log.debug("revokeIdentityJoin, request:{}", request);

        orgInfoService.deleteByPK(request.getMember().getIdentityId());

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokeIdentityJoin response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
