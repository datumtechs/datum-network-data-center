package com.platon.datum.storage.grpc.impl;


import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.common.util.ValueUtils;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.dao.entity.OrgPowerTaskSummary;
import com.platon.datum.storage.dao.entity.PowerServer;
import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.ResourceData;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Resource;
import com.platon.datum.storage.grpc.datacenter.api.ResourceServiceGrpc;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.OrgInfoService;
import com.platon.datum.storage.service.PowerServerService;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class ResourceGrpc extends ResourceServiceGrpc.ResourceServiceImplBase {

    @javax.annotation.Resource
    private PowerServerService powerServerService;

    @javax.annotation.Resource
    private OrgInfoService orgInfoService;

    @javax.annotation.Resource
    private ConvertorService convertorService;

    /**
     * <pre>
     * 存储资源
     * </pre>
     */
    @Transactional
    @Override
    public void publishPower(Resource.PublishPowerRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> publishPowerInternal(input),
                "publishPower");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void publishPowerInternal(Resource.PublishPowerRequest request) {

        ResourceData.ResourcePB power = request.getPower();

        IdentityData.Organization owner = power.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        PowerServer powerServer = new PowerServer();
        powerServer.setIdentityId(owner.getIdentityId());
        powerServer.setDataId(power.getDataId()); //todo:metadata_pb里其实也一样，不过那边加了metadata_id
        powerServer.setDataStatus(CarrierEnum.DataStatus.DataStatus_Valid_VALUE);
        powerServer.setState(CarrierEnum.PowerState.PowerState_Released.ordinal());
        powerServer.setTotalMem(power.getTotalMem());
        powerServer.setUsedMem(power.getUsedMem());
        powerServer.setTotalProcessor(power.getTotalProcessor());
        powerServer.setUsedProcessor(power.getUsedProcessor());
        powerServer.setTotalBandwidth(power.getTotalBandwidth());
        powerServer.setUsedBandwidth(power.getUsedBandwidth());
        powerServer.setTotalDisk(power.getTotalDisk());
        powerServer.setUsedDisk(power.getUsedDisk());
        powerServer.setPublishAt(LocalDateTime.now(ZoneOffset.UTC));
        powerServer.setNonce(power.getNonce());
        powerServerService.insert(powerServer);
    }


    /**
     * <pre>
     * 新增，算力同步，实时通知算力的使用情况（组织下的具体的服务器）
     * </pre>
     */
    @Transactional
    @Override
    public void syncPower(Resource.SyncPowerRequest request,
                          io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> syncPowerInternal(input),
                "syncPower");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void syncPowerInternal(Resource.SyncPowerRequest request) {
        ResourceData.LocalResourcePB power = request.getPower();

        PowerServer powerServer = new PowerServer();
        powerServer.setDataId(power.getDataId());
        powerServer.setState(power.getStateValue());
        powerServer.setUsedProcessor(power.getUsedProcessor());
        powerServer.setUsedMem(power.getUsedMem());
        powerServer.setUsedBandwidth(power.getUsedBandwidth());
        powerServerService.updateByPrimaryKeySelective(powerServer);
    }

    /**
     * <pre>
     * 撤销资源
     * </pre>
     */
    @Transactional
    @Override
    public void revokePower(Resource.RevokePowerRequest request,
                            io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> revokePowerInternal(input),
                "revokePower");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void revokePowerInternal(Resource.RevokePowerRequest request) {

        IdentityData.Organization owner = request.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }
        powerServerService.updateStatus(request.getPowerId(), CarrierEnum.PowerState.PowerState_Revoked.ordinal());
    }

    /**
     * <pre>
     * 新增，用于同步给管理台，获取所有算力资源信息
     * </pre>
     */
    @Override
    public void listPower(Resource.ListPowerRequest request,
                          io.grpc.stub.StreamObserver<Resource.ListPowerResponse> responseObserver) {
        Resource.ListPowerResponse response = GrpcImplUtils.query(
                request,
                input -> listPowerInternal(input),
                bizOut -> Resource.ListPowerResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllPowers(bizOut).build(),
                bizError -> Resource.ListPowerResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Resource.ListPowerResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listPower"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<ResourceData.ResourcePB> listPowerInternal(Resource.ListPowerRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<PowerServer> powerServerList = powerServerService.syncPowerServer(lastUpdateAt, request.getPageSize());

        List<ResourceData.ResourcePB> powerList = powerServerList.parallelStream()
                .map(powerServer -> {
                    try {
                        IdentityData.Organization organization = IdentityData.Organization.newBuilder()
                                .setIdentityId(powerServer.getIdentityId())
                                .setNodeName((String) powerServer.getField("orgName"))
                                .build();

                        return ResourceData.ResourcePB.newBuilder()
                                .setOwner(organization)
                                .setDataId(powerServer.getDataId())
                                .setDataStatus(CarrierEnum.DataStatus.forNumber(powerServer.getDataStatus()))
                                .setState(CarrierEnum.PowerState.forNumber(powerServer.getState()))
                                .setTotalMem(ValueUtils.longValue(powerServer.getTotalMem()))
                                .setUsedMem(ValueUtils.longValue(powerServer.getUsedMem()))
                                .setTotalProcessor(ValueUtils.intValue(powerServer.getTotalProcessor()))
                                .setUsedProcessor(ValueUtils.intValue(powerServer.getUsedProcessor()))
                                .setTotalBandwidth(ValueUtils.longValue(powerServer.getTotalBandwidth()))
                                .setUsedBandwidth(ValueUtils.longValue(powerServer.getUsedBandwidth()))
                                .setTotalDisk(ValueUtils.longValue(powerServer.getTotalDisk()))
                                .setUsedDisk(ValueUtils.longValue(powerServer.getUsedDisk()))
                                .setPublishAt(powerServer.getPublishAt() == null ? 0 : powerServer.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .setUpdateAt(powerServer.getUpdateAt() == null ? 0 : powerServer.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .setNonce(powerServer.getNonce())
                                .build();
                    } catch (Exception exception) {
                        log.error("PowerServer -> ResourcePB error", exception);
                        return null;
                    }
                })
                .filter(resourcePB -> resourcePB != null)
                .collect(Collectors.toList());

        //接口返回值
        return powerList;
    }


    /**
     * <pre>
     * 查看指定节点的总算力摘要
     * </pre>
     */
    @Override
    public void getPowerSummaryByIdentityId(Resource.GetPowerSummaryByIdentityRequest request,
                                            io.grpc.stub.StreamObserver<Resource.PowerSummaryResponse> responseObserver) {

        Resource.PowerSummaryResponse response = GrpcImplUtils.query(
                request,
                input -> getPowerSummaryByIdentityIdInternal(input),
                bizOut -> {
                    String identityId = request.getIdentityId();
                    OrgInfo orgInfo = orgInfoService.findByPK(identityId);
                    if (orgInfo == null) {
                        log.error("identity not found. identityId:={}", identityId);
                        throw new BizException(CodeEnums.ORG_NOT_FOUND);
                    }
                    return Resource.PowerSummaryResponse.newBuilder()
                            .setStatus(CodeEnums.SUCCESS.getCode())
                            .setMsg(CodeEnums.SUCCESS.getMessage())
                            .setOwner(convertorService.toProtoOrganization(orgInfo))
                            .setPowerSummary(Resource.PowerSummary.newBuilder()
                                    .setInformation(ResourceData.ResourceUsageOverview.newBuilder()
                                            .setTotalProcessor(ValueUtils.intValue(bizOut.getCore()))
                                            .setTotalMem(ValueUtils.longValue(bizOut.getMemory()))
                                            .setTotalBandwidth(ValueUtils.longValue(bizOut.getBandwidth()))
                                            .setUsedProcessor(ValueUtils.intValue(bizOut.getUsedCore()))
                                            .setUsedMem(ValueUtils.longValue(bizOut.getUsedMemory()))
                                            .setUsedBandwidth(ValueUtils.longValue(bizOut.getUsedBandwidth()))
                                            .build())
                                    .setState(CarrierEnum.PowerState.forNumber(bizOut.getState()))
                                    .setTotalTaskCount(bizOut.getPowerTaskCount())
                                    .build())
                            .build();
                },
                bizError -> Resource.PowerSummaryResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Resource.PowerSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"getPowerSummaryByIdentityId"
        );

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private OrgPowerTaskSummary getPowerSummaryByIdentityIdInternal(Resource.GetPowerSummaryByIdentityRequest request) {
        OrgPowerTaskSummary powerSummary = powerServerService.getPowerSummaryByOrgId(request.getIdentityId());

        if (powerSummary == null) {
            powerSummary = new OrgPowerTaskSummary();
        }

        return powerSummary;
    }


    /**
     * <pre>
     * 查看各个节点的总算力摘要列表 (不包含 任务描述)(这个是聚合的摘要, 即: 每个组织的总算力, 所以不需要分页
     *
     * 因为这接口的返回值，是个统计数据，ListPowerSummaryRequest中的参数暂时没有意义
     * </pre>
     */
    @Override
    public void listPowerSummary(com.google.protobuf.Empty request,
                                 io.grpc.stub.StreamObserver<Resource.ListPowerSummaryResponse> responseObserver) {
        Resource.ListPowerSummaryResponse response = GrpcImplUtils.query(
                request,
                input -> listPowerSummaryInternal(input),
                bizOut -> Resource.ListPowerSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllPowers(bizOut).build(),
                bizError -> Resource.ListPowerSummaryResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Resource.ListPowerSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listPowerSummary"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Resource.PowerSummaryResponse> listPowerSummaryInternal(com.google.protobuf.Empty request) {

        List<OrgPowerTaskSummary> orgPowerTaskSummaryList = powerServerService.listPowerSummaryGroupByOrgId();

        List<Resource.PowerSummaryResponse> powerTotalSummaryResponseList = orgPowerTaskSummaryList.parallelStream()
                .map(powerSummary -> {
                    try {
                        OrgInfo orgInfo = orgInfoService.findByPK(powerSummary.getIdentityId());
                        if (orgInfo == null) {
                            log.error("power provider identity not found. identityId:={}", powerSummary.getIdentityId());
                            return null;
                        }
                        return Resource.PowerSummaryResponse.newBuilder()
                                .setOwner(convertorService.toProtoOrganization(orgInfo))
                                .setPowerSummary(Resource.PowerSummary.newBuilder()
                                        .setInformation(ResourceData.ResourceUsageOverview.newBuilder()
                                                .setTotalProcessor(ValueUtils.intValue(powerSummary.getCore()))
                                                .setTotalMem(ValueUtils.longValue(powerSummary.getMemory()))
                                                .setTotalBandwidth(ValueUtils.longValue(powerSummary.getBandwidth()))
                                                .setUsedProcessor(ValueUtils.intValue(powerSummary.getUsedCore()))
                                                .setUsedMem(ValueUtils.longValue(powerSummary.getUsedMemory()))
                                                .setUsedBandwidth(ValueUtils.longValue(powerSummary.getUsedBandwidth()))
                                                .build())
                                        .setState(CarrierEnum.PowerState.forNumber(powerSummary.getState()))
                                        .setTotalTaskCount(powerSummary.getPowerTaskCount())
                                        .build())
                                .build();
                    } catch (Exception exception) {
                        log.error("OrgPowerTaskSummary -> PowerSummaryResponse error!", exception);
                        return null;
                    }
                })
                .filter(powerSummaryResponse -> powerSummaryResponse != null)
                .collect(Collectors.toList());

        //结果
        return powerTotalSummaryResponseList;
    }
}
