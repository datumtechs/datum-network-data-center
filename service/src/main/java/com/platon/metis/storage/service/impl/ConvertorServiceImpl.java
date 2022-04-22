package com.platon.metis.storage.service.impl;

import cn.hutool.core.lang.Pair;
import com.google.protobuf.ByteString;
import com.platon.metis.storage.common.exception.OrgNotFound;
import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.metis.storage.grpc.lib.types.Base.*;
import com.platon.metis.storage.grpc.lib.types.MetadataPB;
import com.platon.metis.storage.grpc.lib.types.MetadataSummary;
import com.platon.metis.storage.grpc.lib.types.ResourceUsageOverview;
import com.platon.metis.storage.grpc.lib.types.TaskPB;
import com.platon.metis.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ConvertorServiceImpl implements ConvertorService {

    @Autowired
    private OrgInfoService orgInfoService;

    @Autowired
    private TaskEventService taskEventService;

    @Resource
    private TaskOrgService taskOrgService;

    @Resource
    TaskDataFlowOptionPartService taskDataFlowOptionPartService;

    @Resource
    TaskDataOptionPartService taskDataOptionPartService;

    @Resource
    TaskInnerAlgorithmCodePartService taskInnerAlgorithmCodePartService;

    @Resource
    TaskPowerOptionPartService taskPowerOptionPartService;

    @Resource
    TaskPowerResourceOptionService taskPowerResourceOptionService;


    @Override
    public com.platon.metis.storage.grpc.lib.types.Base.Organization toProtoOrganization(OrgInfo orgInfo) {
        return com.platon.metis.storage.grpc.lib.types.Base.Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getOrgName())
                .setStatus(DataStatus.forNumber(orgInfo.getStatus()))
                .setImageUrl(StringUtils.trimToEmpty(orgInfo.getImageUrl()))
                .setDetails(StringUtils.trimToEmpty(orgInfo.getProfile()))
                .setUpdateAt(orgInfo.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }


    @Override
    public List<com.platon.metis.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.metis.storage.dao.entity.TaskEvent> taskEventList) {
        return taskEventList.stream().map(taskEvent -> {
            return toProtoTaskEvent(taskEvent);
        }).collect(Collectors.toList());
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.TaskEvent toProtoTaskEvent(com.platon.metis.storage.dao.entity.TaskEvent taskEvent) {
        return com.platon.metis.storage.grpc.lib.types.TaskEvent.newBuilder()
                .setTaskId(taskEvent.getTaskId())
                .setType(taskEvent.getEventType())
                .setContent(taskEvent.getEventContent())
                .setCreateAt(taskEvent.getEventAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setIdentityId(taskEvent.getIdentityId())
                .setPartyId(taskEvent.getPartyId())
                .build();
    }

    @Override
    public MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }
        //1.组装整个元数据摘要
        MetadataSummary metadataSummary = MetadataSummary.newBuilder()
                .setMetadataId(dataFile.getMetaDataId())
                .setMetadataName(dataFile.getMetaDataName())
                .setDataType(OrigindataType.forNumber(dataFile.getDataType()))
                .setDesc(StringUtils.trimToEmpty(dataFile.getDesc()))
                .setState(MetadataState.forNumber(dataFile.getStatus()))
                .setIndustry(dataFile.getIndustry())
                .setState(MetadataState.forNumber(dataFile.getStatus()))
                .setPublishAt(dataFile.getPublishAt() == null ? 0 : dataFile.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(dataFile.getUpdateAt() == null ? 0 : dataFile.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();


        return MetadataSummaryOwner.newBuilder()
                .setOwner(this.toProtoOrganization(orgInfo))
                .setInformation(metadataSummary)
                .build();
    }

    @Override
    public List<MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetaDataSummaryWithOwner(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(MetaData dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }
        //1.组装元数据所属组织信息
        Organization owner = Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeName(orgInfo.getOrgName())
                .setNodeId(orgInfo.getNodeId())
                .build();

        //1.组装整个元数据
        return MetadataPB.newBuilder()
                .setOwner(owner)
                .setMetadataId(dataFile.getMetaDataId())
                .setMetadataName(dataFile.getMetaDataName())
                .setDataType(OrigindataType.forNumber(dataFile.getDataType()))
                .setDesc(StringUtils.trimToEmpty(dataFile.getDesc()))
                .setState(MetadataState.forNumber(dataFile.getStatus()))
                .setIndustry(dataFile.getIndustry())
                .setDataStatus(DataStatus.forNumber(dataFile.getDataStatus()))
                .setDataId(dataFile.getDataId())
                .setPublishAt(dataFile.getPublishAt() == null ? 0 : dataFile.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(dataFile.getUpdateAt() == null ? 0 : dataFile.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setMetadataOption(dataFile.getMetaDataOption())
                .build();
    }

    @Override
    public List<MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetadataPB(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public TaskPB toTaskPB(TaskInfo taskInfo) {
        String taskId = taskInfo.getTaskId();

        //初始资源
        TaskResourceCostDeclare taskResourceCostDeclare = TaskResourceCostDeclare.newBuilder()
                .setMemory(taskInfo.getInitMemory())
                .setProcessor(taskInfo.getInitProcessor())
                .setBandwidth(taskInfo.getInitBandwidth())
                .setDuration(taskInfo.getInitDuration())
                .build();

        //参与任务的组织信息
        TaskOrganization senderOrg = null;
        TaskOrganization algoSupplierOrg = null;
        List<TaskOrganization> dataSupplierOrgList = new ArrayList<>();
        List<TaskOrganization> powerSupplierOrgList = new ArrayList<>();
        List<TaskOrganization> receiverOrgList = new ArrayList<>();
        List<TaskOrg> taskOrgList = taskOrgService.findTaskOrgList(taskId);
        for (int i = 0; i < taskOrgList.size(); i++) {
            TaskOrg taskOrg = taskOrgList.get(i);
            TaskOrganization taskOrganization = TaskOrganization.newBuilder()
                    .setPartyId(taskOrg.getPartyId())
                    .setNodeName(taskOrg.getNodeName())
                    .setNodeId(taskOrg.getNodeId())
                    .setIdentityId(taskOrg.getIdentityId())
                    .build();

            TaskOrg.TaskRoleEnum roleEnum = TaskOrg.TaskRoleEnum.getRoleEnum(taskOrg.getTaskRole());
            switch (roleEnum) {
                case sender:
                    senderOrg = taskOrganization;
                    break;
                case algoSupplier:
                    algoSupplierOrg = taskOrganization;
                    break;
                case dataSupplier:
                    dataSupplierOrgList.add(taskOrganization);
                    break;
                case powerSupplier:
                    powerSupplierOrgList.add(taskOrganization);
                    break;
                case receiver:
                    receiverOrgList.add(taskOrganization);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + roleEnum);
            }
        }

        //部分大数据
        String dataFlowPolicyOption = taskDataFlowOptionPartService.getDataFlowOption(taskId);
        String dataPolicyOption = taskDataOptionPartService.getDataOption(taskId);
        Pair<String, String> algorithmPair = taskInnerAlgorithmCodePartService.getAlgorithmCode(taskId);
        String algorithmCode = algorithmPair.getKey();
        String algorithmCodeExtraParams = algorithmPair.getValue();
        String powerPolicyOption = taskPowerOptionPartService.getPowerOption(taskId);
        List<TaskPowerResourceOption> list = taskPowerResourceOptionService.getPowerResourceOption(taskId);
        List<com.platon.metis.storage.grpc.lib.types.TaskPowerResourceOption> powerResourceOptionList = list.stream()
                .map(option -> {
                    ResourceUsageOverview overview = ResourceUsageOverview.newBuilder()
                            .setTotalMem(option.getTotalMemory())
                            .setUsedMem(option.getUsedMemory())
                            .setTotalProcessor(option.getTotalProcessor())
                            .setUsedProcessor(option.getUsedProcessor())
                            .setTotalBandwidth(option.getTotalBandwidth())
                            .setUsedBandwidth(option.getUsedBandwidth())
                            .setTotalDisk(option.getTotalDisk())
                            .setUsedDisk(option.getUsedDisk())
                            .build();

                    com.platon.metis.storage.grpc.lib.types.TaskPowerResourceOption taskPowerResourceOption =
                            com.platon.metis.storage.grpc.lib.types.TaskPowerResourceOption.newBuilder()
                                    .setPartyId(option.getPartId())
                                    .setResourceUsedOverview(overview)
                                    .build();
                    return taskPowerResourceOption;
                }).collect(Collectors.toList());

        //任务相关事件
        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(taskId);

        //组装最终响应体
        return com.platon.metis.storage.grpc.lib.types.TaskPB.newBuilder()
                .setTaskId(taskId)
                .setDataId(taskInfo.getDataId())
                .setDataStatus(DataStatus.forNumber(taskInfo.getDataStatus()))
                .setUser(taskInfo.getUser())
                .setUserType(UserType.forNumber(taskInfo.getUserType()))
                .setTaskName(taskInfo.getTaskName())
                .setSender(senderOrg)
                .setAlgoSupplier(algoSupplierOrg)
                .addAllDataSuppliers(dataSupplierOrgList)
                .addAllPowerSuppliers(powerSupplierOrgList)
                .addAllReceivers(receiverOrgList)
                .setDataPolicyType(taskInfo.getDataPolicyType())
                .setDataPolicyOption(dataPolicyOption)
                .setPowerPolicyType(taskInfo.getPowerPolicyType())
                .setPowerPolicyOption(powerPolicyOption)
                .setDataFlowPolicyType(taskInfo.getDataFlowPolicyType())
                .setDataFlowPolicyOption(dataFlowPolicyOption)
                .setOperationCost(taskResourceCostDeclare)
                .setAlgorithmCode(algorithmCode)
                .setMetaAlgorithmId(taskInfo.getMetaAlgorithmId())
                .setAlgorithmCodeExtraParams(algorithmCodeExtraParams)
                .addAllPowerResourceOptions(powerResourceOptionList)
                .setState(TaskState.forNumber(taskInfo.getState()))
                .setReason(taskInfo.getReason())
                .setDesc(taskInfo.getDesc())
                .setCreateAt(taskInfo.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(taskInfo.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(taskInfo.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .addAllTaskEvents(toProtoTaskEvent(taskEventList))
                .setSign(ByteString.copyFromUtf8(taskInfo.getSign()))
                .setNonce(taskInfo.getNonce())
                .build();
    }

    @Override
    public List<com.platon.metis.storage.grpc.lib.types.TaskPB> toTaskPB(List<TaskInfo> taskInfoList) {
        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList =
                taskInfoList.stream().map(task -> {
                    return toTaskPB(task);
                }).collect(Collectors.toList());
        return grpcTaskList;
    }

}
