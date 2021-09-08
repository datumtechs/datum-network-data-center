package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.common.exception.OrgNotFound;
import com.platon.rosettanet.storage.common.util.ValueUtils;
import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.api.MetadataAuthorityDetail;
import com.platon.rosettanet.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.rosettanet.storage.grpc.lib.common.*;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataAuthority;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataSummary;
import com.platon.rosettanet.storage.grpc.lib.types.ResourceUsageOverview;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.MetaDataService;
import com.platon.rosettanet.storage.service.OrgInfoService;
import com.platon.rosettanet.storage.service.TaskMetaDataColumnService;
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


    /**
     * 对同一个任务的数据提供者进行分类，以便过滤出任务使用的metaData钟的column idx list
     * @param taskMetaDataList
     * @return
     */
    public List<com.platon.rosettanet.storage.grpc.lib.types.TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList){
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


    private com.platon.rosettanet.storage.grpc.lib.types.TaskDataSupplier toProtoDataSupplier(TaskMetaData taskMetaData){
        //meta data column的全集
        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(taskMetaData.getMetaDataId());
        Map<Integer, MetaDataColumn> columnMap = metaDataColumnList.stream().collect(Collectors.toMap(MetaDataColumn::getColumnIdx, obj -> obj));

        //把任务所用meta data column子集的参数补齐
        List<MetadataColumn> metaDataColumnDetailList = taskMetaDataColumnService.listTaskMetaDataColumn(taskMetaData.getTaskId(), taskMetaData.getMetaDataId()).stream()
                .filter(taskMetaDataColumn -> columnMap.get(taskMetaDataColumn.getColumnIdx())!=null)
                .map(taskMetaDataColumn -> {
            MetaDataColumn column = columnMap.get(taskMetaDataColumn.getColumnIdx());
            return MetadataColumn.newBuilder()
                    .setCIndex(column.getColumnIdx())
                    .setCName(column.getColumnName())
                    .setCType(column.getColumnType())
                    .setCSize(column.getColumnSize())
                    .setCComment(StringUtils.trimToEmpty(column.getRemarks()))
                    .build();
        }).collect(Collectors.toList());

        //一个meta data id 属于一个data_file，也属于确定的组织
        OrgInfo orgInfo = orgInfoService.findByPK(taskMetaData.getIdentityId());
        if(orgInfo==null){
            log.error("task (taskId: {}) data (metadataId: {}) provider identity id: {} not found.", taskMetaData.getTaskId(), taskMetaData.getMetaDataId(), taskMetaData.getIdentityId());
            throw new OrgNotFound();
        }
        return com.platon.rosettanet.storage.grpc.lib.types.TaskDataSupplier.newBuilder()
                .setMetadataId(taskMetaData.getMetaDataId())
                .setOrganization(this.toProtoTaskOrganization(orgInfo, taskMetaData.getPartyId()))
                .addAllColumns(metaDataColumnDetailList)
                .build();
    }

    public List<com.platon.rosettanet.storage.grpc.lib.types.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList) {
        //task的power可以为空，当task执行失败时，可能没有power
        if (CollectionUtils.isEmpty(taskPowerProviderList)){
            return new ArrayList<>();
        }
        return taskPowerProviderList.parallelStream().map(taskPowerProvider -> {

            OrgInfo orgInfo = orgInfoService.findByPK(taskPowerProvider.getIdentityId());
            if(orgInfo==null){
                log.error("task (taskId: {}) power provider identity id: {} not found.", taskPowerProvider.getTaskId(), taskPowerProvider.getIdentityId());
                throw new OrgNotFound();
            }
            return com.platon.rosettanet.storage.grpc.lib.types.TaskPowerSupplier.newBuilder()
                    .setOrganization(this.toProtoTaskOrganization(orgInfo, taskPowerProvider.getPartyId()))
                    .setResourceUsedOverview(ResourceUsageOverview.newBuilder()
                            .setUsedProcessor(ValueUtils.intValue(taskPowerProvider.getUsedCore()))
                            .setUsedMem(ValueUtils.longValue(taskPowerProvider.getUsedMemory()))
                            .setUsedBandwidth(ValueUtils.longValue(taskPowerProvider.getUsedBandwidth())))
                    .build();
        }).collect(Collectors.toList());
    }

    public List<com.platon.rosettanet.storage.grpc.lib.common.TaskOrganization> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList) {
        return taskResultConsumerList.parallelStream().map(item -> {
            OrgInfo consumer = orgInfoService.findByPK(item.getConsumerIdentityId());
            return this.toProtoTaskOrganization(consumer, item.getConsumerPartyId());
        }).collect(Collectors.toList());


    }


    public com.platon.rosettanet.storage.grpc.lib.common.Organization toProtoOrganization(OrgInfo orgInfo){
        return com.platon.rosettanet.storage.grpc.lib.common.Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setNodeName(orgInfo.getOrgName())
                .setStatus(CommonStatus.forNumber(orgInfo.getStatus()))
                .build();
    }

    public com.platon.rosettanet.storage.grpc.lib.common.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId){
        return TaskOrganization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setPartyId(partyId)
                .setNodeName(orgInfo.getOrgName())
                .build();
    }


    public List<com.platon.rosettanet.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.rosettanet.storage.dao.entity.TaskEvent> taskEventList){
        return taskEventList.parallelStream().map(taskEvent -> {
            return toProtoTaskEvent(taskEvent);
        }).collect(Collectors.toList());
    }

    public com.platon.rosettanet.storage.grpc.lib.types.TaskEvent toProtoTaskEvent(com.platon.rosettanet.storage.dao.entity.TaskEvent taskEvent){
        return com.platon.rosettanet.storage.grpc.lib.types.TaskEvent.newBuilder()
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
        if(orgInfo==null){
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
        return dataFileList.parallelStream().map(dataFile->{
            return this.toProtoMetaDataSummaryOwner(dataFile);
        }).filter(item -> item!=null).collect(Collectors.toList());
    }

    @Override
    public MetadataAuthorityDetail toProtoMetaDataAuthorityResponse(MetaDataAuth metaDataAuth) {
        return MetadataAuthorityDetail.newBuilder()
                .setMetadataAuthId(metaDataAuth.getMetaDataAuthId())
                .setUser(metaDataAuth.getUserId())
                .setUserType(UserType.forNumber(metaDataAuth.getUserType()))
                .setApplyAt(metaDataAuth.getApplyAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setAudit(AuditMetadataOption.forNumber(metaDataAuth.getStatus()))
                .setAuditAt(metaDataAuth.getApplyAt()==null?0:metaDataAuth.getAuditAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setAuth(MetadataAuthority.newBuilder()
                        .setMetadataId(metaDataAuth.getMetaDataId())
                        .setOwner(Organization.newBuilder().setIdentityId(metaDataAuth.getUserIdentityId()).build()))
                .build();
    }
}
