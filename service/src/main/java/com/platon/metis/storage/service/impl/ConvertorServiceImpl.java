package com.platon.metis.storage.service.impl;

import com.platon.metis.storage.common.exception.OrgNotFound;
import com.platon.metis.storage.common.exception.TaskMetaDataNotFound;
import com.platon.metis.storage.common.util.ValueUtils;
import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.metis.storage.grpc.lib.common.*;
import com.platon.metis.storage.grpc.lib.types.*;
import com.platon.metis.storage.service.*;
import lombok.extern.slf4j.Slf4j;
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
        return taskMetaDataList.parallelStream().map(taskMetaData -> {
            return toProtoDataSupplier(taskMetaData);
        }).collect(Collectors.toList());
    }


    private com.platon.metis.storage.grpc.lib.types.TaskDataSupplier toProtoDataSupplier(TaskMetaData taskMetaData) {
        //meta data column的全集
        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(taskMetaData.getMetaDataId());
        Map<Integer, MetaDataColumn> columnMap = metaDataColumnList.stream().collect(Collectors.toMap(MetaDataColumn::getColumnIdx, obj -> obj));

        //把任务所用meta data column子集的参数补齐
        List<MetadataColumn> metaDataColumnDetailList = taskMetaDataColumnService.listTaskMetaDataColumn(taskMetaData.getTaskId(), taskMetaData.getMetaDataId()).stream()
                .filter(taskMetaDataColumn -> columnMap.get(taskMetaDataColumn.getSelectedColumnIdx()) != null)
                .map(taskMetaDataColumn -> {
                    MetaDataColumn column = columnMap.get(taskMetaDataColumn.getSelectedColumnIdx());
                    return toProtoMetadataColumn(column);
                }).collect(Collectors.toList());

        //一个meta data id 属于一个data_file，也属于确定的组织
        OrgInfo orgInfo = orgInfoService.findByPK(taskMetaData.getIdentityId());
        if (orgInfo == null) {
            log.error("task (taskId: {}) data (metadataId: {}) provider identity id: {} not found.", taskMetaData.getTaskId(), taskMetaData.getMetaDataId(), taskMetaData.getIdentityId());
            throw new OrgNotFound();
        }
        return com.platon.metis.storage.grpc.lib.types.TaskDataSupplier.newBuilder()
                .setMetadataId(taskMetaData.getMetaDataId())
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
        return taskPowerProviderList.parallelStream().map(taskPowerProvider -> {

            OrgInfo orgInfo = orgInfoService.findByPK(taskPowerProvider.getIdentityId());
            if (orgInfo == null) {
                log.error("task (taskId: {}) power provider identity id: {} not found.", taskPowerProvider.getTaskId(), taskPowerProvider.getIdentityId());
                throw new OrgNotFound();
            }
            return com.platon.metis.storage.grpc.lib.types.TaskPowerSupplier.newBuilder()
                    .setOrganization(this.toProtoTaskOrganization(orgInfo, taskPowerProvider.getPartyId()))
                    .setResourceUsedOverview(ResourceUsageOverview.newBuilder()
                            .setUsedProcessor(ValueUtils.intValue(taskPowerProvider.getUsedCore()))
                            .setUsedMem(ValueUtils.longValue(taskPowerProvider.getUsedMemory()))
                            .setUsedBandwidth(ValueUtils.longValue(taskPowerProvider.getUsedBandwidth())))
                    .build();
        }).collect(Collectors.toList());
    }

    public List<com.platon.metis.storage.grpc.lib.common.TaskOrganization> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList) {
        return taskResultConsumerList.parallelStream().map(item -> {
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
        return taskEventList.parallelStream().map(taskEvent -> {
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
                        .setFileType(OriginFileType.valueOf(dataFile.getFileType()))
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
        return dataFileList.parallelStream().map(dataFile -> {
            return this.toProtoMetaDataSummaryOwner(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(DataFile dataFile) {
        /*OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if(orgInfo==null){
            log.error("identity not found. identityId:={}", dataFile.getIdentityId());
            throw new OrgNotFound();
        }
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        */

        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(dataFile.getMetaDataId());
        List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.parallelStream().map(column -> {
            return toProtoMetaDataColumnDetail(column);
        }).collect(Collectors.toList());

        return MetadataPB.newBuilder()
                .setIdentityId(dataFile.getIdentityId())
                .setOriginId(dataFile.getOriginId())
                .setMetadataId(dataFile.getMetaDataId())
                .setTableName(dataFile.getFileName())
                .setFilePath(dataFile.getFilePath())
                .setFileType(OriginFileType.valueOf(dataFile.getFileType()))
                .setHasTitle(dataFile.getHasTitle())
                .setSize(dataFile.getSize().intValue())
                .setRows(dataFile.getRows().intValue())
                .setColumns(dataFile.getColumns())
                .setDesc(StringUtils.trimToEmpty(dataFile.getRemarks()))
                .setState(MetadataState.forNumber(dataFile.getStatus()))
                .addAllMetadataColumns(metaDataColumnDetailList)
                .build();
    }

    @Override
    public List<MetadataPB> toProtoMetadataPB(List<DataFile> dataFileList) {
        return dataFileList.parallelStream().map(dataFile -> {
            return this.toProtoMetadataPB(dataFile);
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth) {
        return MetadataAuthorityPB.newBuilder()
                .setMetadataAuthId(metaDataAuth.getMetaDataAuthId())
                .setUser(metaDataAuth.getUserId())
                .setUserType(UserType.forNumber(metaDataAuth.getUserType()))
                .setApplyAt(metaDataAuth.getApplyAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setAuditOption(AuditMetadataOption.forNumber(metaDataAuth.getStatus()))
                .setAuditAt(metaDataAuth.getApplyAt() == null ? 0 : metaDataAuth.getAuditAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setAuth(MetadataAuthority.newBuilder()
                        .setMetadataId(metaDataAuth.getMetaDataId())
                        .setOwner(Organization.newBuilder().setIdentityId(metaDataAuth.getUserIdentityId()).build()))
                .build();
    }

    @Override
    public List<com.platon.metis.storage.grpc.lib.types.TaskPB> toTaskPB(List<Task> taskList) {
        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList =
                taskList.parallelStream().map(task -> {
                    return toTaskPB(task);
                }).collect(Collectors.toList());
        return grpcTaskList;
    }

    @Override
    public com.platon.metis.storage.grpc.lib.types.TaskPB toTaskPB(Task task) {

        //算法提供者
        TaskAlgoProvider taskAlgoProvider = taskAlgoProviderService.findAlgoProviderByTaskId(task.getId());
        OrgInfo taskAlgoProviderOrgInfo = orgInfoService.findByPK(taskAlgoProvider.getIdentityId());

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

        return com.platon.metis.storage.grpc.lib.types.TaskPB.newBuilder()
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(TaskState.forNumber(task.getStatus())) //todo: to check is right
                .setIdentityId(task.getOwnerIdentityId())
                .setPartyId(task.getOwnerPartyId())
                .setAlgoSupplier(toProtoTaskOrganization(taskAlgoProviderOrgInfo, taskAlgoProvider.getPartyId()))
                .setOperationCost(com.platon.metis.storage.grpc.lib.common.TaskResourceCostDeclare.newBuilder().setProcessor(task.getRequiredCore()).setMemory(task.getRequiredMemory()).setBandwidth(task.getRequiredBandwidth()).setDuration(task.getRequiredDuration()).build())
                .addAllDataSuppliers(toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSuppliers(toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(toProtoResultReceiver(taskResultConsumerList))
                .build();
    }
}
