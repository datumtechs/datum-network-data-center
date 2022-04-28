package com.platon.metis.storage.service.impl;

import cn.hutool.core.lang.Pair;
import com.google.protobuf.ByteString;
import com.platon.metis.storage.common.exception.OrgNotFound;
import com.platon.metis.storage.dao.entity.TaskEvent;
import com.platon.metis.storage.dao.entity.TaskPowerResourceOption;
import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.metis.storage.grpc.lib.types.Base.*;
import com.platon.metis.storage.grpc.lib.types.*;
import com.platon.metis.storage.service.*;
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
    public IdentityPB toProtoIdentityPB(OrgInfo orgInfo) {
        return IdentityPB.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getNodeName())
                .setDataId(orgInfo.getDataId())
                .setDataStatus(DataStatus.forNumber(orgInfo.getDataStatus()))
                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                .setCredential(orgInfo.getCredential())
                .setImageUrl(orgInfo.getImageUrl())
                .setDetails(orgInfo.getDetails())
                .setNonce(orgInfo.getNonce())
                .setUpdateAt(orgInfo.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }

    @Override
    public Organization toProtoOrganization(OrgInfo orgInfo) {
        return Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getNodeName())
                .setDataStatus(DataStatus.forNumber(orgInfo.getDataStatus()))
                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                .setImageUrl(orgInfo.getImageUrl())
                .setDetails(orgInfo.getDetails())
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
                .setNodeName(orgInfo.getNodeName())
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
        List<String> dataFlowPolicyOption = taskDataFlowOptionPartService.getDataFlowOption(taskId);
        List<String> dataPolicyOption = taskDataOptionPartService.getDataOption(taskId);
        Pair<String, String> algorithmPair = taskInnerAlgorithmCodePartService.getAlgorithmCode(taskId);
        String algorithmCode = algorithmPair.getKey();
        String algorithmCodeExtraParams = algorithmPair.getValue();
        List<String> powerPolicyOption = taskPowerOptionPartService.getPowerOption(taskId);
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
                .addAllDataFlowPolicyTypes(taskInfo.getDataPolicyTypesList())
                .addAllDataPolicyOptions(dataPolicyOption)
                .addAllPowerPolicyTypes(taskInfo.getPowerPolicyTypesList())
                .addAllPowerPolicyOptions(powerPolicyOption)
                .addAllDataFlowPolicyTypes(taskInfo.getDataFlowPolicyTypesList())
                .addAllDataFlowPolicyOptions(dataFlowPolicyOption)
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

    @Override
    public MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth) {
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

        return MetadataAuthorityPB.newBuilder()
                .setMetadataAuthId(metaDataAuth.getMetaDataAuthId())
                .setUser(metaDataAuth.getUser())
                .setDataId(metaDataAuth.getDataId())
                .setDataStatus(DataStatus.forNumber(metaDataAuth.getDataStatus()))
                .setUserType(UserType.forNumber(metaDataAuth.getUserType()))
                .setAuth(MetadataAuthority.newBuilder()
                        .setMetadataId(metaDataAuth.getMetaDataId())
                        .setOwner(Organization.newBuilder()
                                .setIdentityId(metaDataAuth.getIdentityId())
                                .setNodeId(orgInfo.getNodeId())
                                .setNodeName(orgInfo.getNodeName())
                                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                                .build())
                        .setUsageRule(MetadataUsageRule.newBuilder()
                                .setUsageType(MetadataUsageType.forNumber(metaDataAuth.getUsageType()))
                                .setTimes(metaDataAuth.getTimes())
                                .setStartAt(metaDataAuth.getStartAt() == null ? 0 : metaDataAuth.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .setEndAt(metaDataAuth.getEndAt() == null ? 0 : metaDataAuth.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .build())
                )
                .setAuditOption(AuditMetadataOption.forNumber(metaDataAuth.getAuditOption()))
                .setAuditSuggestion(StringUtils.trimToEmpty(metaDataAuth.getAuditSuggestion()))
                .setUsedQuo(MetadataUsedQuo.newBuilder().setUsageType(MetadataUsageType.forNumber(metaDataAuth.getUsageType()))
                        .setExpire(metaDataAuth.getExpire() == 1 ? true : false)
                        .setUsedTimes(metaDataAuth.getUsedTimes())
                        .build())

                .setApplyAt(metaDataAuth.getApplyAt() == null ? 0 : metaDataAuth.getApplyAt().toInstant(ZoneOffset.UTC).toEpochMilli())

                .setAuditAt(metaDataAuth.getAuditAt() == null ? 0 : metaDataAuth.getAuditAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(MetadataAuthorityState.forNumber(metaDataAuth.getState()))
                .setSign(sign)
                .setPublishAt(metaDataAuth.getPublishAt() == null ? 0 : metaDataAuth.getPublishAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(metaDataAuth.getUpdateAt() == null ? 0 : metaDataAuth.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setNonce(metaDataAuth.getNonce())
                .build();
    }

}
