package com.platon.rosettanet.storage.service.impl;

import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.*;
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
    public List<com.platon.rosettanet.storage.grpc.lib.TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList){
        return taskMetaDataList.stream().map(taskMetaData -> {
            return toProtoDataSupplier(taskMetaData);
        }).collect(Collectors.toList());
    }


    private TaskDataSupplier toProtoDataSupplier(TaskMetaData taskMetaData){
        //meta data column的全集
        List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(taskMetaData.getMetaDataId());
        Map<Integer, MetaDataColumn> columnMap = metaDataColumnList.stream().collect(Collectors.toMap(MetaDataColumn::getColumnIdx, obj -> obj));

        //把任务所用meta data column子集的参数补齐
        List<MetaDataColumnDetail> metaDataColumnDetailList = taskMetaDataColumnService.listTaskMetaDataColumn(taskMetaData.getTaskId(), taskMetaData.getMetaDataId()).stream().map(taskMetaDataColumn -> {
            MetaDataColumn column = columnMap.get(taskMetaDataColumn.getColumnIdx());
            return MetaDataColumnDetail.newBuilder()
                    .setCindex(column.getColumnIdx())
                    .setCname(column.getColumnName())
                    .setCtype(column.getColumnType())
                    .setCsize(column.getColumnSize())
                    .setCcomment(StringUtils.trimToEmpty(column.getRemarks()))
                    .build();
        }).collect(Collectors.toList());

        //一个meta data id 属于一个data_file，也属于确定的组织
        OrgInfo orgInfo = orgInfoService.findByMetaDataId(taskMetaData.getMetaDataId());
        return TaskDataSupplier.newBuilder()
                .setMetaId(taskMetaData.getMetaDataId())
                .setMemberInfo(this.toProtoTaskOrganization(orgInfo, taskMetaData.getPartyId()))
                .addAllColumnMeta(metaDataColumnDetailList)
                .build();
    }

    public List<com.platon.rosettanet.storage.grpc.lib.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList) {
        return taskPowerProviderList.stream().map(taskPowerProvider -> {

            OrgInfo orgInfo = orgInfoService.findByPK(taskPowerProvider.getIdentityId());

            return TaskPowerSupplier.newBuilder()
                    .setMemberInfo(this.toProtoTaskOrganization(orgInfo, taskPowerProvider.getPartyId()))
                    .setPowerInfo(ResourceUsedDetail.newBuilder()
                            .setUsedProcessor(taskPowerProvider.getUsedCore())
                            .setUsedMem(taskPowerProvider.getUsedMemory())
                            .setUsedBandwidth(taskPowerProvider.getUsedBandwidth()))
                    .build();
        }).collect(Collectors.toList());
    }

    public List<com.platon.rosettanet.storage.grpc.lib.TaskResultReceiver> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList) {
        Map<String, List<TaskResultConsumer>> byConsumerIdMap = taskResultConsumerList.stream()
                .collect(Collectors.groupingBy(TaskResultConsumer::getConsumerIdentityId));

        List<TaskResultReceiver> taskResultReceiverList = new ArrayList<>();
        for (String consumerId: byConsumerIdMap.keySet()) {
            if (CollectionUtils.isEmpty(byConsumerIdMap.get(consumerId))){
                continue;
            }
            //默认同一组的consumer是一样的
            String consumerPartyId  = byConsumerIdMap.get(consumerId).get(0).getConsumerPartyId();

            //结果消费者
            OrgInfo consumerOrgInfo = orgInfoService.findByPK(consumerId);

            //结果产生者
            List<TaskOrganization> resultProducerList = byConsumerIdMap.get(consumerId).stream().map(item -> {
                OrgInfo producer = orgInfoService.findByPK(item.getProducerIdentityId());
                return this.toProtoTaskOrganization(producer, item.getProducerPartyId());
            }).collect(Collectors.toList());

            taskResultReceiverList.add( TaskResultReceiver.newBuilder()
                    .setMemberInfo(this.toProtoTaskOrganization(consumerOrgInfo, consumerPartyId))
                    .addAllProvider(resultProducerList)
                    .build());
        }
        return taskResultReceiverList;
    }


    public com.platon.rosettanet.storage.grpc.lib.Organization toProtoOrganization(OrgInfo orgInfo){
        return Organization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setName(orgInfo.getOrgName())
                .build();
    }

    public com.platon.rosettanet.storage.grpc.lib.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId){
        return TaskOrganization.newBuilder()
                .setIdentityId(orgInfo.getIdentityId())
                .setNodeId(orgInfo.getNodeId())
                .setPartyId(partyId)
                .setName(orgInfo.getOrgName())
                .build();
    }


    public List<com.platon.rosettanet.storage.grpc.lib.TaskEvent> toProtoTaskEvent(List<com.platon.rosettanet.storage.dao.entity.TaskEvent> taskEventList){
        return taskEventList.stream().map(taskEvent -> {
            return toProtoTaskEvent(taskEvent);
        }).collect(Collectors.toList());
    }

    public com.platon.rosettanet.storage.grpc.lib.TaskEvent toProtoTaskEvent(com.platon.rosettanet.storage.dao.entity.TaskEvent taskEvent){
        return com.platon.rosettanet.storage.grpc.lib.TaskEvent.newBuilder()
                .setTaskId(taskEvent.getTaskId())
                .setType(taskEvent.getEventType())
                .setContent(taskEvent.getEventContent())
                .setCreateAt(taskEvent.getEventAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setOwner(Organization.newBuilder().setIdentityId(taskEvent.getIdentityId()).build())
                .build();
    }

    @Override
    public MetaDataColumnDetail toProtoMetaDataColumnDetail(MetaDataColumn metaDataColumn) {
        return MetaDataColumnDetail.newBuilder()
                .setCindex(metaDataColumn.getColumnIdx())
                .setCname(metaDataColumn.getColumnName())
                .setCtype(metaDataColumn.getColumnType())
                .setCsize(metaDataColumn.getColumnSize())
                .setCcomment(StringUtils.trimToEmpty(metaDataColumn.getRemarks()))
                .build();
    }

    @Override
    public MetaDataSummaryOwner toProtoMetaDataSummaryOwner(DataFile dataFile) {
        OrgInfo orgInfo = orgInfoService.findByPK(dataFile.getIdentityId());
        if(orgInfo==null){
            return null;
        }

        return MetaDataSummaryOwner.newBuilder()
                .setOwner(this.toProtoOrganization(orgInfo))
                .setInformation(MetaDataSummary.newBuilder()
                        .setOriginId(dataFile.getId())
                        .setMetaDataId(dataFile.getMetaDataId())
                        .setTableName(dataFile.getFileName())
                        .setFilePath(dataFile.getFilePath())
                        .setFileType(dataFile.getFileType())
                        .setHasTitle(dataFile.getHasTitle())
                        .setSize(dataFile.getSize())
                        .setRows(dataFile.getRows().intValue())
                        .setColumns(dataFile.getColumns())
                        .setDesc(StringUtils.trimToEmpty(dataFile.getRemarks()))
                        .setState(dataFile.getStatus())
                        .build())
                .build();
    }

    @Override
    public List<MetaDataSummaryOwner> toProtoMetaDataSummaryOwner(List<DataFile> dataFileList) {
        return dataFileList.parallelStream().map(dataFile->{
            return this.toProtoMetaDataSummaryOwner(dataFile);
        }).filter(item -> item!=null).collect(Collectors.toList());
    }
}
