package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.common.exception.OrgNotFound;
import com.platon.rosettanet.storage.common.util.ValueUtils;
import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.rosettanet.storage.dao.entity.PowerServer;
import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.PowerState;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import com.platon.rosettanet.storage.grpc.lib.types.Power;
import com.platon.rosettanet.storage.grpc.lib.types.ResourceUsageOverview;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.OrgInfoService;
import com.platon.rosettanet.storage.service.PowerServerService;
import com.platon.rosettanet.storage.service.TaskPowerProviderService;
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
public class ResourceGrpc extends ResourceServiceGrpc.ResourceServiceImplBase {

    @Autowired
    private PowerServerService powerServerService;

    @Autowired
    private OrgInfoService orgInfoService;

    @Autowired
    private ConvertorService convertorService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    /**
     * <pre>
     * 存储资源
     * </pre>
     */
    public void publishPower(com.platon.rosettanet.storage.grpc.lib.api.PublishPowerRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("publishPower, request:{}", request);

        PowerServer powerServer = new PowerServer();
        powerServer.setId(request.getPowerId());
        powerServer.setIdentityId(request.getOwner().getIdentityId());

        powerServer.setCore(request.getInformation().getProcessor());
        powerServer.setMemory(request.getInformation().getMem());
        powerServer.setBandwidth(request.getInformation().getBandwidth());
        powerServer.setPublished(true);
        powerServer.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        powerServer.setStatus(PowerState.PowerState_Released.ordinal());
        powerServerService.insert(powerServer);

        //接口返回值
        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("publishPower, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 新增，算力同步，实时通知算力的使用情况（组织下的具体的服务器）
     * </pre>
     */
    public void syncPower(com.platon.rosettanet.storage.grpc.lib.api.SyncPowerRequest request,
                          io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("syncPower, request:{}", request);

        PowerServer powerServer = new PowerServer();
        powerServer.setId(request.getPower().getPowerId());

        powerServer.setUsedCore(request.getPower().getUsageOverview().getUsedProcessor());
        powerServer.setUsedMemory(request.getPower().getUsageOverview().getUsedMem());
        powerServer.setUsedBandwidth(request.getPower().getUsageOverview().getUsedBandwidth());
        powerServerService.updateByPrimaryKeySelective(powerServer);

        //接口返回值
        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("syncPower, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 撤销资源
     * </pre>
     */
    public void revokePower(com.platon.rosettanet.storage.grpc.lib.api.RevokePowerRequest request,
                            io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("revokePower, request:{}", request);

        powerServerService.updateStatus(request.getPowerId(), PowerState.PowerState_Revoked.ordinal());

        //接口返回值
        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokePower, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增，用于同步给管理台，获取所有算力资源信息
     * </pre>
     */
    public void getPowerList(com.platon.rosettanet.storage.grpc.lib.api.PowerListRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.PowerListResponse> responseObserver) {

        log.debug("getPowerList, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<PowerServer> powerServerList = powerServerService.syncPowerServer(lastUpdateAt);

        List<Power> powerList = powerServerList.parallelStream().map(powerServer -> {
            return Power.newBuilder()
                    .setPowerId(powerServer.getId())

                    .setUsageOverview(ResourceUsageOverview.newBuilder()
                            .setTotalProcessor(ValueUtils.intValue(powerServer.getCore()))
                            .setTotalMem(ValueUtils.longValue(powerServer.getUsedMemory()))
                            .setTotalBandwidth(ValueUtils.longValue(powerServer.getUsedBandwidth()))
                            .setUsedProcessor(ValueUtils.intValue(powerServer.getUsedCore()))
                            .setUsedMem(ValueUtils.longValue(powerServer.getUsedMemory()))
                            .setUsedBandwidth(ValueUtils.longValue(powerServer.getUsedBandwidth()))
                            .build())
                    .build();
        }).collect(Collectors.toList());

        //接口返回值
        PowerListResponse response = PowerListResponse.newBuilder().addAllPowers(powerList).build();

        log.debug("getPowerList, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查看指定节点的总算力摘要
     * </pre>
     */
    public void getPowerSummaryByIdentityId(com.platon.rosettanet.storage.grpc.lib.api.PowerSummaryByIdentityRequest request,
                                        io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.PowerTotalSummaryResponse> responseObserver) {
        log.debug("getPowerSummaryByNodeId, request:{}", request);

        String identityId = request.getIdentityId();
        OrgInfo owner = orgInfoService.findByPK(identityId);

        if(owner==null){
            log.error("identity not found. identityId:={}", identityId);
            throw new OrgNotFound();
        }

        int taskCounts = taskPowerProviderService.countTaskAsPowerProvider(identityId);

        PowerServer powerServer = powerServerService.countPowerByOrgId(identityId);

        if (powerServer==null){
            powerServer = new PowerServer();
        }

        PowerTotalSummaryResponse response = PowerTotalSummaryResponse.newBuilder()
                .setOwner(convertorService.toProtoOrganization(owner))
                .setPowerTotalSummary(PowerTotalSummary.newBuilder()
                        .setInformation(ResourceUsageOverview.newBuilder()
                                .setTotalProcessor(ValueUtils.intValue(powerServer.getCore()))
                                .setTotalMem(ValueUtils.longValue(powerServer.getMemory()))
                                .setTotalBandwidth(ValueUtils.longValue(powerServer.getBandwidth()))
                                .setUsedProcessor(ValueUtils.intValue(powerServer.getUsedCore()))
                                .setUsedMem(ValueUtils.longValue(powerServer.getUsedMemory()))
                                .setUsedBandwidth(ValueUtils.longValue(powerServer.getUsedBandwidth()))
                                .build())
                        .setTotalTaskCount(taskCounts)
                        .build())
                .build();

        log.debug("getPowerSummaryByNodeId, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    /**
     * <pre>
     * 查看各个节点的总算力摘要列表 (不包含 任务描述)
     * </pre>
     */
    public void getPowerTotalSummaryList(com.google.protobuf.Empty request,
                                         io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.PowerTotalSummaryListResponse> responseObserver) {

        log.debug("getPowerTotalSummaryList, request:{}", request);

        List<OrgPowerTaskSummary> orgPowerTaskSummaryList = powerServerService.countPowerGroupByOrgId();

        List<PowerTotalSummaryResponse> powerTotalSummaryResponseList = orgPowerTaskSummaryList.parallelStream().map(orgPowerTaskSummary -> {
            OrgInfo orgInfo = orgInfoService.findByPK(orgPowerTaskSummary.getIdentityId());
            if(orgInfo==null){
                log.error("power provider identity not found. identityId:={}", orgPowerTaskSummary.getIdentityId());
                throw new OrgNotFound();
            }
            return PowerTotalSummaryResponse.newBuilder()
                    .setOwner(convertorService.toProtoOrganization(orgInfo))
                    .setPowerTotalSummary(PowerTotalSummary.newBuilder()
                            .setInformation(ResourceUsageOverview.newBuilder()
                                    .setTotalProcessor(ValueUtils.intValue(orgPowerTaskSummary.getCore()))
                                    .setTotalMem(ValueUtils.longValue(orgPowerTaskSummary.getMemory()))
                                    .setTotalBandwidth(ValueUtils.longValue(orgPowerTaskSummary.getBandwidth()))
                                    .setUsedProcessor(ValueUtils.intValue(orgPowerTaskSummary.getUsedCore()))
                                    .setUsedMem(ValueUtils.longValue(orgPowerTaskSummary.getUsedMemory()))
                                    .setUsedBandwidth(ValueUtils.longValue(orgPowerTaskSummary.getUsedBandwidth()))
                                    .build())
                            .setTotalTaskCount(ValueUtils.intValue(orgPowerTaskSummary.getPowerTaskCount()))
                            .build())
                    .build();
        }).collect(Collectors.toList());

        //结果
        PowerTotalSummaryListResponse response = PowerTotalSummaryListResponse.newBuilder()
                .addAllPowers(powerTotalSummaryResponseList)
                .build();

        log.debug("getPowerTotalSummaryList, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
