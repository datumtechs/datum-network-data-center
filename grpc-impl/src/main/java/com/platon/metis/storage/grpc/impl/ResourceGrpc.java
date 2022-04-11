package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.common.exception.OrgNotFound;
import com.platon.metis.storage.common.util.ValueUtils;
import com.platon.metis.storage.dao.entity.OrgInfo;
import com.platon.metis.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.metis.storage.dao.entity.PowerServer;
import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.types.Base;
import com.platon.metis.storage.grpc.lib.types.ResourcePB;
import com.platon.metis.storage.grpc.lib.types.ResourceUsageOverview;
import com.platon.metis.storage.service.ConvertorService;
import com.platon.metis.storage.service.OrgInfoService;
import com.platon.metis.storage.service.PowerServerService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * <pre>
     * 存储资源
     * </pre>
     */
    @Transactional
    public void publishPower(com.platon.metis.storage.grpc.lib.api.PublishPowerRequest request,
                             io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("publishPower, request:{}", request);

        PowerServer powerServer = new PowerServer();
        powerServer.setId(request.getPower().getDataId()); //todo:metadata_pb里其实也一样，不过那边加了metadata_id
        powerServer.setIdentityId(request.getPower().getOwner().getIdentityId());

        powerServer.setCore(request.getPower().getTotalProcessor());
        powerServer.setMemory(request.getPower().getTotalMem());
        powerServer.setBandwidth(request.getPower().getTotalBandwidth());
        powerServer.setPublished(true);
        powerServer.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));

        powerServer.setStatus(Base.PowerState.PowerState_Released.ordinal());

        powerServerService.insert(powerServer);

        //接口返回值
        Base.SimpleResponse response = Base.SimpleResponse.newBuilder().setStatus(0).build();

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
    @Transactional
    public void syncPower(com.platon.metis.storage.grpc.lib.api.SyncPowerRequest request,
                          io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("syncPower, request:{}", request);

        PowerServer powerServer = new PowerServer();
        powerServer.setId(request.getPower().getDataId());
        powerServer.setStatus(request.getPower().getStateValue());
        powerServer.setUsedCore(request.getPower().getUsedProcessor());
        powerServer.setUsedMemory(request.getPower().getUsedMem());
        powerServer.setUsedBandwidth(request.getPower().getUsedBandwidth());
        powerServerService.updateByPrimaryKeySelective(powerServer);

        //接口返回值
        Base.SimpleResponse response = Base.SimpleResponse.newBuilder().setStatus(0).build();

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
    @Transactional
    public void revokePower(com.platon.metis.storage.grpc.lib.api.RevokePowerRequest request,
                            io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("revokePower, request:{}", request);

        powerServerService.updateStatus(request.getPowerId(), Base.PowerState.PowerState_Revoked.ordinal());

        //接口返回值
        Base.SimpleResponse response = Base.SimpleResponse.newBuilder().setStatus(0).build();

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
    public void listPower(com.platon.metis.storage.grpc.lib.api.ListPowerRequest request,
                             io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListPowerResponse> responseObserver) {

        log.debug("listPower, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<PowerServer> powerServerList = powerServerService.syncPowerServer(lastUpdateAt, request.getPageSize());

        List<ResourcePB> powerList = powerServerList.parallelStream().map(powerServer -> {
            Base.Organization organization = Base.Organization.newBuilder()
                    .setIdentityId(powerServer.getIdentityId())
                    .setNodeName((String)powerServer.getField("orgName"))
                    .build();

            return ResourcePB.newBuilder()
                    .setDataId(powerServer.getId())
                    .setOwner(organization)
                    .setState(Base.PowerState.forNumber(powerServer.getStatus()))
                    .setTotalProcessor(ValueUtils.intValue(powerServer.getCore()))
                    .setTotalMem(ValueUtils.longValue(powerServer.getMemory()))
                    .setTotalBandwidth(ValueUtils.longValue(powerServer.getBandwidth()))
                    .setUsedProcessor(ValueUtils.intValue(powerServer.getUsedCore()))
                    .setUsedMem(ValueUtils.longValue(powerServer.getUsedMemory()))
                    .setUsedBandwidth(ValueUtils.longValue(powerServer.getUsedBandwidth()))
                    .setPublishAt(powerServer.getPublishedAt()==null?0:powerServer.getPublishedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .setUpdateAt(powerServer.getUpdateAt()==null?0:powerServer.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .build();
        }).collect(Collectors.toList());

        //接口返回值
        ListPowerResponse response = ListPowerResponse.newBuilder().addAllPowers(powerList).build();

        log.debug("listPower, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查看指定节点的总算力摘要
     * </pre>
     */
    public void getPowerSummaryByIdentityId(com.platon.metis.storage.grpc.lib.api.GetPowerSummaryByIdentityRequest request,
                                        io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.PowerSummaryResponse> responseObserver) {
        log.debug("getPowerSummaryByIdentityId, request:{}", request);

        String identityId = request.getIdentityId();
        OrgInfo orgInfo = orgInfoService.findByPK(identityId);

        if(orgInfo==null){
            log.error("identity not found. identityId:={}", identityId);
            throw new OrgNotFound();
        }

        //int taskCounts = taskPowerProviderService.countTaskAsPowerProvider(identityId);

        OrgPowerTaskSummary powerSummary = powerServerService.getPowerSummaryByOrgId(identityId);

        if (powerSummary==null){
            powerSummary = new OrgPowerTaskSummary();
        }

        PowerSummaryResponse response = PowerSummaryResponse.newBuilder()
                .setOwner(convertorService.toProtoOrganization(orgInfo))
                .setPowerSummary(PowerSummary.newBuilder()
                        .setInformation(ResourceUsageOverview.newBuilder()
                                .setTotalProcessor(ValueUtils.intValue(powerSummary.getCore()))
                                .setTotalMem(ValueUtils.longValue(powerSummary.getMemory()))
                                .setTotalBandwidth(ValueUtils.longValue(powerSummary.getBandwidth()))
                                .setUsedProcessor(ValueUtils.intValue(powerSummary.getUsedCore()))
                                .setUsedMem(ValueUtils.longValue(powerSummary.getUsedMemory()))
                                .setUsedBandwidth(ValueUtils.longValue(powerSummary.getUsedBandwidth()))
                                .build())
                        .setTotalTaskCount(powerSummary.getPowerTaskCount())
                        .build())
                .build();

        log.debug("getPowerSummaryByIdentityId, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查看各个节点的总算力摘要列表 (不包含 任务描述)(这个是聚合的摘要, 即: 每个组织的总算力, 所以不需要分页
     *
     * 因为这接口的返回值，是个统计数据，ListPowerSummaryRequest中的参数暂时没有意义
     * </pre>
     */
    public void listPowerSummary(com.google.protobuf.Empty request,
                                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListPowerSummaryResponse> responseObserver) {

        log.debug("getPowerTotalSummaryList, request:{}", request);

        List<OrgPowerTaskSummary> orgPowerTaskSummaryList = powerServerService.listPowerSummaryGroupByOrgId();

        List<PowerSummaryResponse> powerTotalSummaryResponseList = orgPowerTaskSummaryList.parallelStream().map(powerSummary -> {
            OrgInfo orgInfo = orgInfoService.findByPK(powerSummary.getIdentityId());
            if(orgInfo==null){
                log.error("power provider identity not found. identityId:={}", powerSummary.getIdentityId());
                throw new OrgNotFound();
            }
            return PowerSummaryResponse.newBuilder()
                    .setOwner(convertorService.toProtoOrganization(orgInfo))
                    .setPowerSummary(PowerSummary.newBuilder()
                            .setInformation(ResourceUsageOverview.newBuilder()
                                    .setTotalProcessor(ValueUtils.intValue(powerSummary.getCore()))
                                    .setTotalMem(ValueUtils.longValue(powerSummary.getMemory()))
                                    .setTotalBandwidth(ValueUtils.longValue(powerSummary.getBandwidth()))
                                    .setUsedProcessor(ValueUtils.intValue(powerSummary.getUsedCore()))
                                    .setUsedMem(ValueUtils.longValue(powerSummary.getUsedMemory()))
                                    .setUsedBandwidth(ValueUtils.longValue(powerSummary.getUsedBandwidth()))
                                    .build())
                            .setTotalTaskCount(powerSummary.getPowerTaskCount())
                            .build())
                    .build();
        }).collect(Collectors.toList());

        //结果
        ListPowerSummaryResponse response = ListPowerSummaryResponse.newBuilder()
                .addAllPowers(powerTotalSummaryResponseList)
                .build();

        log.debug("getPowerTotalSummaryList, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
