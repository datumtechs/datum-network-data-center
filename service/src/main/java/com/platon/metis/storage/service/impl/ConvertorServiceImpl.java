package com.platon.metis.storage.service.impl;

import com.google.protobuf.ByteString;
import com.platon.metis.storage.common.exception.MetaDataNotFound;
import com.platon.metis.storage.common.exception.OrgNotFound;
import com.platon.metis.storage.common.exception.TaskMetaDataNotFound;
import com.platon.metis.storage.common.util.ValueUtils;
import com.platon.metis.storage.dao.entity.TaskEvent;
import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.metis.storage.grpc.lib.common.*;
import com.platon.metis.storage.grpc.lib.types.*;
import com.platon.metis.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ConvertorServiceImpl implements ConvertorService {

    @Autowired
    private OrgInfoService orgInfoService;
    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private TaskMetaDataColumnService taskMetaDataColumnService;

    @Autowired
    private TaskAlgoProviderService taskAlgoProviderService;

    @Autowired
    private TaskMetaDataService taskMetaDataService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    @Autowired
    private TaskResultConsumerService taskResultConsumerService;
    @Autowired
    private TaskEventService taskEventService;

    @Autowired
    private PowerServerService powerServerService;

    /**
     * 对同一个任务的数据提供者进行分类，以便过滤出任务使用的metaData钟的column idx list
     *
     * @param taskMetaDataList
     * @return
     */
    public List<com.platon.metis.storage.grpc.lib.types.TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList) {
        /*List<TaskDataSupplier> taskDataSupplierList = new ArrayList<>();
        for (TaskMetaData taskMetaData: taskMetaDataList) {
            TaskDataSupplier taskDataSupplier = toProtoDataSupplier(taskMetaData);
            taskDataSupplierList.add(taskDataSupplier);
        }
        return taskDataSupplierList;*/
        return taskMetaDataList.stream().map(taskMetaData -> {
            return toProtoDataSupplier(taskMetaData);
        }).collect(Collectors.toList());
    }


    private com.platon.metis.storage.grpc.lib.types.TaskDataSupplier toProtoDataSupplier(TaskMetaData taskMetaData) {

        //一个meta data id 属于一个data_file，也属于确定的组织
        OrgInfo orgInfo = orgInfoService.findByPK(taskMetaData.getIdentityId());
        if (orgInfo == null) {
            log.error("task (taskId: {}) data (metadataId: {}) provider identity id: {} not found.", taskMetaData.getTaskId(), taskMetaData.getMetaDataId(), taskMetaData.getIdentityId());
            throw new OrgNotFound();
        }


        //meta data column的全集
        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(taskMetaData.getMetaDataId());
        if(metaDataColumnList==null || metaDataColumnList.size()==0){
            log.warn("there's no task medata columns.");
            return com.platon.metis.storage.grpc.lib.types.TaskDataSupplier.newBuilder()
                    .setMetadataId(taskMetaData.getMetaDataId())
                    .setMetadataName("")
                    .setOrganization(this.toProtoTaskOrganization(orgInfo, taskMetaData.getPartyId()))
                    .setKeyColumn(MetadataColumn.newBuilder())   //主键列
                    .addAllSelectedColumns(new ArrayList<>())    //参与计算列
                    .build();
        }

        Map<Integer, MetaDataColumn> columnMap = metaDataColumnList.stream().collect(Collectors.toMap(MetaDataColumn::getColumnIdx, obj -> obj));

        //把任务所用meta data column子集的参数补齐
        List<MetadataColumn> metaDataColumnDetailList = taskMetaDataColumnService.listTaskMetaDataColumn(taskMetaData.getTaskId(), taskMetaData.getMetaDataId()).stream()
                .filter(taskMetaDataColumn -> columnMap.get(taskMetaDataColumn.getSelectedColumnIdx()) != null)
                .map(taskMetaDataColumn -> {
                    MetaDataColumn column = columnMap.get(taskMetaDataColumn.getSelectedColumnIdx());
                    return toProtoMetadataColumn(column);
                }).collect(Collectors.toList());


        DataFile dataFile = metaDataService.findByMetaDataId(taskMetaData.getMetaDataId());
        if(dataFile==null){
            log.error("metadata not found, metadataId:{}", taskMetaData.getMetaDataId());
            throw new MetaDataNotFound();
        }

        return com.platon.metis.storage.grpc.lib.types.TaskDataSupplier.newBuilder()
                .setMetadataId(dataFile.getMetaDataId())
                .setMetadataName(dataFile.getResourceName())
                .setOrganization(this.toProtoTaskOrganization(orgInfo, taskMetaData.getPartyId()))
                .setKeyColumn(toProtoMetadataColumn(columnMap.get(taskMetaData.getKeyColumnIdx()))) //主键列
                .addAllSelectedColumns(metaDataColumnDetailList)    //参与计算列
                .build();
    }

    private com.platon.metis.storage.grpc.lib.types.MetadataColumn toProtoMetadataColumn(MetaDataColumn column){
        return MetadataColumn.newBuilder()
                .setCIndex(column.getColumnIdx())
                .setCName(column.getColumnName())
                .setCType(column.getColumnType())
                .setCSize(column.getColumnSize())
                .setCComment(StringUtils.trimToEmpty(column.getRemarks()))
                .build();
    }

    public List<com.platon.metis.storage.grpc.lib.types.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList) {
        //task的power可以为空，当task执行失败时，可能没有power
        if (CollectionUtils.isEmpty(taskPowerProviderList)) {
            return new ArrayList<>();
        }
        return taskPowerProviderList.stream().map(taskPowerProvider -> {


            OrgInfo orgInfo = orgInfoService.findByPK(taskPowerProvider.getIdentityId());
            if (orgInfo == null) {
                log.error("task (taskId: {}) power provider identity id: {} not found.", taskPowerProvider.getTaskId(), taskPowerProvider.getIdentityId());
                throw new OrgNotFound();
            }


            PowerServer powerServer = powerServerService.sumPowerByOrgId(taskPowerProvider.getIdentityId());
            return com.platon.metis.storage.grpc.lib.types.TaskPowerSupplier.newBuilder()
                    .setOrganization(this.toProtoTaskOrganization(orgInfo, taskPowerProvider.getPartyId()))
                    .setResourceUsedOverview(ResourceUsageOverview.newBuilder()
                            .setTotalProcessor(ValueUtils.intValue(powerServer.getCore()))
                            .setTotalMem(ValueUtils.longValue(powerServer.getMemory()))
                            .setTotalBandwidth(ValueUtils.longValue(powerServer.getBandwidth()))
                            .setUsedProcessor(ValueUtils.intValue(taskPowerProvider.getUsedCore()))
                            .setUsedMem(ValueUtils.longValue(taskPowerProvider.getUsedMemory()))
                            .setUsedBandwidth(ValueUtils.longValue(taskPowerProvider.getUsedBandwidth())))
                    .build();
        }).collect(Collectors.toList());
    }

    public List<com.platon.metis.storage.grpc.lib.common.TaskOrganization> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList) {
        return taskResultConsumerList.stream().map(item -> {
            OrgInfo consumer = orgInfoService.findByPK(item.getConsumerIdentityId());
            return this.toProtoTaskOrganization(consumer, item.getConsumerPartyId());
        }).collect(Collectors.toList());


    }


    public com.platon.metis.storage.grpc.lib.common.Organization toProtoOrganization(OrgInfo orgInfo) {
        return com.platon.metis.storage.grpc.lib.common.Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getOrgName())
                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                .build();
    }

    public com.platon.metis.storage.grpc.lib.common.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId) {
        return TaskOrganization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setPartyId(partyId)
                .setNodeName(orgInfo.getOrgName())
                .build();
    }


    public List<com.platon.metis.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.metis.storage.dao.entity.TaskEvent> taskEventList) {
        return taskEventList.stream().map(taskEvent -> {
            return toProtoTaskEvent(taskEvent);
        }).collect(Collectors.toList());
    }

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
    public MetadataColumn toProtoMetaDataColumnDetail(MetaDataColumn metaDataColumn) {
        return MetadataColumn.newBuilder()
                .setCIndex(metaDataColumn.getColumnIdx())
                .setCName(metaDataColumn.getColumnName())
                .setCType(metaDataColumn.getColumnType())
                .setCSize(metaDataColumn.getColumnSize())
                .setCComment(StringUtils.trimToEmpty(metaDataColumn.getRemarks()))
                .build();
    }

    @Override
    public MetadataSummaryOwner toProtoMetaDataSummaryOwner(DataFile dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }

        return MetadataSummaryOwner.newBuilder()
                .setOwner(this.toProtoOrganization(orgInfo))
                .setInformation(MetadataSummary.newBuilder()
                        .setOriginId(dataFile.getOriginId())
                        .setMetadataId(dataFile.getMetaDataId())
                        .setTableName(dataFile.getFileName())
                        .setFilePath(dataFile.getFilePath())
                        .setFileType(OriginFileType.forNumber(dataFile.getFileType()))
                        .setHasTitle(dataFile.getHasTitle())
                        .setSize(dataFile.getSize().intValue())
                        .setRows(dataFile.getRows().intValue())
                        .setColumns(dataFile.getColumns())
                        .setDesc(StringUtils.trimToEmpty(dataFile.getRemarks()))
                        .setState(MetadataState.forNumber(dataFile.getStatus()))
                        .build())
                .build();
    }

    @Override
    public List<MetadataSummaryOwner> toProtoMetaDataSummaryOwner(List<DataFile> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetaDataSummaryOwner(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(DataFile dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if(orgInfo==null){
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }

        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(dataFile.getMetaDataId());
        List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.stream().map(column -> {
            return toProtoMetaDataColumnDetail(column);
        }).collect(Collectors.toList());

        return MetadataPB.newBuilder()
                .setIdentityId(dataFile.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getOrgName())
                .setOriginId(dataFile.getOriginId())
                .setMetadataId(dataFile.getMetaDataId())
                .setTableName(dataFile.getFileName())
                .setFilePath(dataFile.getFilePath())
                .setFileType(OriginFileType.forNumber(dataFile.getFileType()))
                .setHasTitle(dataFile.getHasTitle())
                .setSize(dataFile.getSize().intValue())
                .setRows(dataFile.getRows().intValue())
                .setColumns(dataFile.getColumns())
                .setDesc(StringUtils.trimToEmpty(dataFile.getRemarks()))
                .setState(MetadataState.forNumber(dataFile.getStatus()))
                .setIndustry(dataFile.getIndustry())
                .setDataStatus(DataStatus.forNumber(dataFile.getDfsDataStatus()))
                .setDataId(dataFile.getDfsDataId())
                .setPublishAt(dataFile.getPublishedAt()==null?0:dataFile.getPublishedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setUpdateAt(dataFile.getUpdateAt()==null?0:dataFile.getUpdateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .addAllMetadataColumns(metaDataColumnDetailList)
                .build();
    }

    @Override
    public List<MetadataPB> toProtoMetadataPB(List<DataFile> dataFileList) {
        return dataFileList.stream().map(dataFile -> {
            return this.toProtoMetadataPB(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth) {
        ByteString sign = ByteString.EMPTY;
        if(StringUtils.isNotEmpty(metaDataAuth.getAuthSign())){
            try {
                 sign = ByteString.copyFrom(Hex.decodeHex(metaDataAuth.getAuthSign()));
            } catch (DecoderException e) {
                log.error("cannot decode the sign", e);
            }
        }

        OrgInfo orgInfo = orgInfoService.findByPK(metaDataAuth.getUserIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", metaDataAuth.getUserIdentityId());
            throw new OrgNotFound();
        }

        return MetadataAuthorityPB.newBuilder()
                .setMetadataAuthId(metaDataAuth.getMetaDataAuthId())
                .setUser(metaDataAuth.getUserId())
                .setDataId(metaDataAuth.getDfsDataId())
                .setDataStatus(DataStatus.forNumber(metaDataAuth.getDfsDataStatus()))
                .setUserType(UserType.forNumber(metaDataAuth.getUserType()))
                .setAuth(MetadataAuthority.newBuilder()
                        .setMetadataId(metaDataAuth.getMetaDataId())
                        .setOwner(Organization.newBuilder()
                                .setIdentityId(metaDataAuth.getUserIdentityId())
                                .setNodeId(orgInfo.getNodeId())
                                .setNodeName(orgInfo.getOrgName())
                                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                                .build())
                        .setUsageRule(MetadataUsageRule.newBuilder()
                                .setUsageType(MetadataUsageType.forNumber(metaDataAuth.getAuthType()))
                                .setTimes(metaDataAuth.getTimes())
                                .setStartAt(metaDataAuth.getStartAt()==null?0:metaDataAuth.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .setEndAt(metaDataAuth.getEndAt()==null?0:metaDataAuth.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                                .build())
                )
                .setAuditOption(AuditMetadataOption.forNumber(metaDataAuth.getAuditOption()))
                .setAuditSuggestion(StringUtils.trimToEmpty(metaDataAuth.getAuditDesc()))
                .setUsedQuo(MetadataUsedQuo.newBuilder().setUsageType(MetadataUsageType.forNumber(metaDataAuth.getAuthType()))
                        .setExpire(metaDataAuth.getExpired())
                        .setUsedTimes(metaDataAuth.getUsedTimes())
                        .build())

                .setApplyAt(metaDataAuth.getApplyAt()==null?0:metaDataAuth.getApplyAt().toInstant(ZoneOffset.UTC).toEpochMilli())

                .setAuditAt(metaDataAuth.getAuditAt() == null ? 0 : metaDataAuth.getAuditAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(MetadataAuthorityState.forNumber(metaDataAuth.getAuthStatus()))
                .setSign(sign)
                .build();
    }

    @Override
    public List<com.platon.metis.storage.grpc.lib.types.TaskPB> toTaskPB(List<Task> taskList) {
        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList =
                taskList.stream().map(task -> {
                    return toTaskPB(task);
                }).collect(Collectors.toList());
        return grpcTaskList;
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.TaskPB toTaskPB(Task task) {

        //算法提供者
        TaskAlgoProvider taskAlgoProvider = taskAlgoProviderService.findAlgoProviderByTaskId(task.getId());
        OrgInfo taskAlgoProviderOrgInfo = orgInfoService.findByPK(taskAlgoProvider.getIdentityId());

        OrgInfo owner =  orgInfoService.findByPK(task.getOwnerIdentityId());


        List<TaskMetaData> taskMetaDataList = taskMetaDataService.listTaskMetaData(task.getId());
        if (CollectionUtils.isEmpty(taskMetaDataList)) {
            log.error("task metadata not found. taskId:={}", task.getId());
            throw new TaskMetaDataNotFound();
        }

        List<TaskPowerProvider> taskPowerProviderList = taskPowerProviderService.listTaskPowerProvider(task.getId());
        //task 可以没有power
        /*if(CollectionUtils.isEmpty(taskPowerProviderList)){
            log.error("task power not found. taskId:={}", task.getId());
            throw new TaskPowerNotFound();
        }*/

        List<TaskResultConsumer> taskResultConsumerList = taskResultConsumerService.listTaskResultConsumer(task.getId());

        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(task.getId());

        ByteString sign = ByteString.EMPTY;
        if(StringUtils.isNotEmpty(task.getTaskSign())){
            try {
                sign = ByteString.copyFrom(Hex.decodeHex(task.getTaskSign()));
            } catch (DecoderException e) {
                log.error("cannot decode the task sign", e);
            }
        }

        return com.platon.metis.storage.grpc.lib.types.TaskPB.newBuilder()
                .setIdentityId(owner.getIdentityId())
                .setNodeId(owner.getNodeId())
                .setNodeName(owner.getOrgName())
                //.setDataId("")
                //.setDataStatus(DataStatus.DataStatus_Unknown)
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setState(TaskState.forNumber(task.getStatus())) //todo: to check is right
                .setReason(StringUtils.trimToEmpty(task.getStatusDesc()))
                .setEventCount(taskEventList.size())
                .setDesc(StringUtils.trimToEmpty(task.getRemarks()))
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPartyId(task.getOwnerPartyId())
                .setAlgoSupplier(toProtoTaskOrganization(taskAlgoProviderOrgInfo, taskAlgoProvider.getPartyId()))
                .setOperationCost(com.platon.metis.storage.grpc.lib.common.TaskResourceCostDeclare.newBuilder().setProcessor(task.getRequiredCore()).setMemory(task.getRequiredMemory()).setBandwidth(task.getRequiredBandwidth()).setDuration(task.getRequiredDuration()).build())
                .addAllDataSuppliers(toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSuppliers(toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(toProtoResultReceiver(taskResultConsumerList))
                .addAllTaskEvents(toProtoTaskEvent(taskEventList))
                .setUser(task.getUserId())
                .setUserType(UserType.forNumber(task.getUserType()))
                .setSign(sign)
                .build();
    }
}
