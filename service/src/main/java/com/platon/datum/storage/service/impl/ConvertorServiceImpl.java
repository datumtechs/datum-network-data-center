package com.platon.datum.storage.service.impl;

import cn.hutool.core.lang.Pair;
import com.google.protobuf.ByteString;
import com.platon.datum.storage.common.exception.OrgNotFound;
import com.platon.datum.storage.dao.entity.*;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.ResourceData;
import com.platon.datum.storage.grpc.carrier.types.TaskData;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Metadata;
import com.platon.datum.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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
    private TaskDataFlowOptionPartService taskDataFlowOptionPartService;

    @Resource
    private TaskDataOptionPartService taskDataOptionPartService;

    @Resource
    private TaskInnerAlgorithmCodePartService taskInnerAlgorithmCodePartService;

    @Resource
    private TaskPowerOptionPartService taskPowerOptionPartService;

    @Resource
    private TaskPowerResourceOptionsService taskPowerResourceOptionsService;

    @Resource
    private TaskReceiverOptionService taskReceiverOptionService;


    @Override
    public IdentityData.IdentityPB toProtoIdentityPB(OrgInfo orgInfo) {
        return IdentityData.IdentityPB.newBuilder()
                .setIdeneityTypeValue(orgInfo.getIdentityType())
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getNodeName())
                .setDataId(orgInfo.getDataId())
                .setDataStatus(CarrierEnum.DataStatus.forNumber(orgInfo.getDataStatus()))
                .setStatus(CarrierEnum.CommonStatus.forNumber(orgInfo.getStatus()))
                .setCredential(orgInfo.getCredential())
                .setImageUrl(orgInfo.getImageUrl())
                .setDetails(orgInfo.getDetails())
                .setNonce(orgInfo.getNonce())
                .setUpdateAt(orgInfo.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }

    @Override
    public IdentityData.Organization toProtoOrganization(OrgInfo orgInfo) {
        return IdentityData.Organization.newBuilder()
                .setIdeneityTypeValue(orgInfo.getIdentityType())
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getNodeName())
                .setDataStatus(CarrierEnum.DataStatus.forNumber(orgInfo.getDataStatus()))
                .setStatus(CarrierEnum.CommonStatus.forNumber(orgInfo.getStatus()))
                .setImageUrl(orgInfo.getImageUrl())
                .setDetails(orgInfo.getDetails())
                .setUpdateAt(orgInfo.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }


    @Override
    public List<com.platon.datum.storage.grpc.carrier.types.TaskData.TaskEvent> toProtoTaskEvent(List<com.platon.datum.storage.dao.entity.TaskEvent> taskEventList) {
        return taskEventList.stream().map(taskEvent -> {
            return toProtoTaskEvent(taskEvent);
        }).collect(Collectors.toList());
    }

    @Override
    public com.platon.datum.storage.grpc.carrier.types.TaskData.TaskEvent toProtoTaskEvent(com.platon.datum.storage.dao.entity.TaskEvent taskEvent) {
        return com.platon.datum.storage.grpc.carrier.types.TaskData.TaskEvent.newBuilder()
                .setTaskId(taskEvent.getTaskId())
                .setType(taskEvent.getEventType())
                .setContent(taskEvent.getEventContent())
                .setCreateAt(taskEvent.getEventAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setIdentityId(taskEvent.getIdentityId())
                .setPartyId(taskEvent.getPartyId())
                .build();
    }

    @Override
    public Metadata.MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }
        //1.组装整个元数据摘要
        com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataSummary metadataSummary = com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataSummary.newBuilder()
                .setMetadataId(dataFile.getMetaDataId())
                .setMetadataName(dataFile.getMetaDataName())
                .setDataType(CarrierEnum.OrigindataType.forNumber(dataFile.getDataType()))
                .setDesc(StringUtils.trimToEmpty(dataFile.getDesc()))
                .setState(CarrierEnum.MetadataState.forNumber(dataFile.getStatus()))
                .setIndustry(dataFile.getIndustry())
                .setState(CarrierEnum.MetadataState.forNumber(dataFile.getStatus()))
                .setPublishAt(dataFile.getPublishAt() == null ? 0 : dataFile.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(dataFile.getUpdateAt() == null ? 0 : dataFile.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setLocationType(CarrierEnum.DataLocationType.forNumber(dataFile.getLocationType()))
                .setUser(dataFile.getUser())
                .setUserTypeValue(dataFile.getUserType())
                .setSign(ByteString.copyFromUtf8(dataFile.getSign()))
                .build();


        return Metadata.MetadataSummaryOwner.newBuilder()
                .setOwner(this.toProtoOrganization(orgInfo))
                .setInformation(metadataSummary)
                .build();
    }

    @Override
    public List<Metadata.MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetaDataSummaryWithOwner(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataPB toProtoMetadataPB(MetaData dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }
        //1.组装元数据所属组织信息
        IdentityData.Organization owner = IdentityData.Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeName(orgInfo.getNodeName())
                .setNodeId(orgInfo.getNodeId())
                .build();

        //1.组装整个元数据
        return com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataPB.newBuilder()
                .setOwner(owner)
                .setMetadataId(dataFile.getMetaDataId())
                .setMetadataName(dataFile.getMetaDataName())
                .setDataType(CarrierEnum.OrigindataType.forNumber(dataFile.getDataType()))
                .setDesc(StringUtils.trimToEmpty(dataFile.getDesc()))
                .setState(CarrierEnum.MetadataState.forNumber(dataFile.getStatus()))
                .setIndustry(dataFile.getIndustry())
                .setDataStatus(CarrierEnum.DataStatus.forNumber(dataFile.getDataStatus()))
                .setDataId(dataFile.getDataId())
                .setPublishAt(dataFile.getPublishAt() == null ? 0 : dataFile.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(dataFile.getUpdateAt() == null ? 0 : dataFile.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setMetadataOption(dataFile.getMetaDataOption())
                .setLocationType(CarrierEnum.DataLocationType.forNumber(dataFile.getLocationType()))
                .setUser(dataFile.getUser())
                .setUserTypeValue(dataFile.getUserType())
                .setSign(ByteString.copyFromUtf8(dataFile.getSign()))
                .build();
    }

    @Override
    public List<com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetadataPB(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public TaskData.TaskPB toTaskPB(TaskInfo taskInfo) {
        String taskId = taskInfo.getTaskId();

        //初始资源
        ResourceData.TaskResourceCostDeclare taskResourceCostDeclare = ResourceData.TaskResourceCostDeclare.newBuilder()
                .setMemory(taskInfo.getInitMemory())
                .setProcessor(taskInfo.getInitProcessor())
                .setBandwidth(taskInfo.getInitBandwidth())
                .setDuration(taskInfo.getInitDuration())
                .build();

        //参与任务的组织信息
        TaskData.TaskOrganization senderOrg = null;
        TaskData.TaskOrganization algoSupplierOrg = null;
        List<TaskData.TaskOrganization> dataSupplierOrgList = new ArrayList<>();
        List<TaskData.TaskOrganization> powerSupplierOrgList = new ArrayList<>();
        List<TaskData.TaskOrganization> receiverOrgList = new ArrayList<>();
        List<TaskOrg> taskOrgList = taskOrgService.findTaskOrgList(taskId);
        for (int i = 0; i < taskOrgList.size(); i++) {
            TaskOrg taskOrg = taskOrgList.get(i);
            TaskData.TaskOrganization taskOrganization = TaskData.TaskOrganization.newBuilder()
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

        //大字段属性
        List<String> dataFlowPolicyOption = taskDataFlowOptionPartService.getDataFlowOption(taskId);
        List<String> dataPolicyOption = taskDataOptionPartService.getDataOption(taskId);
        Pair<String, String> algorithmPair = taskInnerAlgorithmCodePartService.getAlgorithmCode(taskId);
        String algorithmCode = algorithmPair.getKey();
        String algorithmCodeExtraParams = algorithmPair.getValue();
        List<String> powerPolicyOption = taskPowerOptionPartService.getPowerOption(taskId);
        List<TaskPowerResourceOptions> list = taskPowerResourceOptionsService.getPowerResourceOption(taskId);
        List<com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPowerResourceOption> powerResourceOptionList = list.stream()
                .map(option -> {
                    ResourceData.ResourceUsageOverview overview = ResourceData.ResourceUsageOverview.newBuilder()
                            .setTotalMem(option.getTotalMemory())
                            .setUsedMem(option.getUsedMemory())
                            .setTotalProcessor(option.getTotalProcessor())
                            .setUsedProcessor(option.getUsedProcessor())
                            .setTotalBandwidth(option.getTotalBandwidth())
                            .setUsedBandwidth(option.getUsedBandwidth())
                            .setTotalDisk(option.getTotalDisk())
                            .setUsedDisk(option.getUsedDisk())
                            .build();

                    com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPowerResourceOption taskPowerResourceOption =
                            com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPowerResourceOption.newBuilder()
                                    .setPartyId(option.getPartId())
                                    .setResourceUsedOverview(overview)
                                    .build();
                    return taskPowerResourceOption;
                }).collect(Collectors.toList());
        List<String> receiverOption = taskReceiverOptionService.getReceiverOption(taskId);


        //任务相关事件
        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(taskId);

        //组装最终响应体
        return com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPB.newBuilder()
                .setTaskId(taskId)
                .setDataId(taskInfo.getDataId())
                .setDataStatus(CarrierEnum.DataStatus.forNumber(taskInfo.getDataStatus()))
                .setUser(taskInfo.getUser())
                .setUserType(CarrierEnum.UserType.forNumber(taskInfo.getUserType()))
                .setTaskName(taskInfo.getTaskName())
                .setSender(senderOrg)
                .setAlgoSupplier(algoSupplierOrg)
                .addAllDataSuppliers(dataSupplierOrgList)
                .addAllPowerSuppliers(powerSupplierOrgList)
                .addAllReceivers(receiverOrgList)
                .addAllDataPolicyTypes(taskInfo.getDataPolicyTypesList())
                .addAllDataPolicyOptions(dataPolicyOption)
                .addAllPowerPolicyTypes(taskInfo.getPowerPolicyTypesList())
                .addAllPowerPolicyOptions(powerPolicyOption)
                .addAllDataFlowPolicyTypes(taskInfo.getDataFlowPolicyTypesList())
                .addAllDataFlowPolicyOptions(dataFlowPolicyOption)
                .addAllReceiverPolicyTypes(taskInfo.getReceiverPolicyTypesList())
                .addAllReceiverPolicyOptions(receiverOption)
                .setOperationCost(taskResourceCostDeclare)
                .setAlgorithmCode(algorithmCode)
                .setMetaAlgorithmId(taskInfo.getMetaAlgorithmId())
                .setAlgorithmCodeExtraParams(algorithmCodeExtraParams)
                .addAllPowerResourceOptions(powerResourceOptionList)
                .setState(CarrierEnum.TaskState.forNumber(taskInfo.getState()))
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
    public List<com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPB> toTaskPB(List<TaskInfo> taskInfoList) {
        List<com.platon.datum.storage.grpc.carrier.types.TaskData.TaskPB> grpcTaskList = taskInfoList.stream()
                .map(this::toTaskPB)
                .collect(Collectors.toList());
        return grpcTaskList;
    }

    @Override
    public com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth) {
        ByteString sign = ByteString.EMPTY;
        if (StringUtils.isNotEmpty(metaDataAuth.getSign())) {
            try {
                sign = ByteString.copyFrom(Hex.decodeHex(metaDataAuth.getSign()));
            } catch (DecoderException e) {
                log.error("cannot decode the sign", e);
            }
        }

        OrgInfo orgInfo = orgInfoService.findByPK(metaDataAuth.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", metaDataAuth.getIdentityId());
            throw new OrgNotFound();
        }

        return com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataAuthorityPB.newBuilder()
                .setMetadataAuthId(metaDataAuth.getMetaDataAuthId())
                .setUser(metaDataAuth.getUser())
                .setDataId(metaDataAuth.getDataId())
                .setDataStatus(CarrierEnum.DataStatus.forNumber(metaDataAuth.getDataStatus()))
                .setUserType(CarrierEnum.UserType.forNumber(metaDataAuth.getUserType()))
                .setAuth(com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataAuthority.newBuilder()
                        .setMetadataId(metaDataAuth.getMetaDataId())
                        .setOwner(IdentityData.Organization.newBuilder()
                                .setIdentityId(metaDataAuth.getIdentityId())
                                .setNodeId(orgInfo.getNodeId())
                                .setNodeName(orgInfo.getNodeName())
                                .setStatus(CarrierEnum.CommonStatus.forNumber(orgInfo.getStatus()))
                                .build())
                        .setUsageRule(com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataUsageRule.newBuilder()
                                .setUsageType(CarrierEnum.MetadataUsageType.forNumber(metaDataAuth.getUsageType()))
                                .setTimes(metaDataAuth.getTimes())
                                .setStartAt(metaDataAuth.getStartAt() == null ? 0 : metaDataAuth.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .setEndAt(metaDataAuth.getEndAt() == null ? 0 : metaDataAuth.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .build())
                )
                .setAuditOption(CarrierEnum.AuditMetadataOption.forNumber(metaDataAuth.getAuditOption()))
                .setAuditSuggestion(StringUtils.trimToEmpty(metaDataAuth.getAuditSuggestion()))
                .setUsedQuo(com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataUsedQuo.newBuilder().setUsageType(CarrierEnum.MetadataUsageType.forNumber(metaDataAuth.getUsageType()))
                        .setExpire(metaDataAuth.getExpire() == 1 ? true : false)
                        .setUsedTimes(metaDataAuth.getUsedTimes())
                        .build())

                .setApplyAt(metaDataAuth.getApplyAt() == null ? 0 : metaDataAuth.getApplyAt().toInstant(ZoneOffset.UTC).toEpochMilli())

                .setAuditAt(metaDataAuth.getAuditAt() == null ? 0 : metaDataAuth.getAuditAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(CarrierEnum.MetadataAuthorityState.forNumber(metaDataAuth.getState()))
                .setSign(sign)
                .setPublishAt(metaDataAuth.getPublishAt() == null ? 0 : metaDataAuth.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(metaDataAuth.getUpdateAt() == null ? 0 : metaDataAuth.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setNonce(metaDataAuth.getNonce())
                .build();
    }

}
