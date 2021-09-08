package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.DataFile;
import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;
import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.MetadataState;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.MetaDataService;
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
public class MetaDataGrpc extends MetadataServiceGrpc.MetadataServiceImplBase {

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private ConvertorService convertorService;

    /**
     * <pre>
     * 保存元数据
     * </pre>
     */
    public void metadataSave(com.platon.rosettanet.storage.grpc.lib.api.MetadataSaveRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("metaDataSave, request:{}", request);

        DataFile dataFile = new DataFile();
        dataFile.setOriginId(request.getMetaSummary().getOriginId());
        dataFile.setMetaDataId(request.getMetaSummary().getMetadataId());
        dataFile.setFileName(request.getMetaSummary().getTableName());
        dataFile.setFilePath(request.getMetaSummary().getFilePath());
        dataFile.setFileType(request.getMetaSummary().getFileType().name());
        dataFile.setIdentityId(request.getOwner().getIdentityId());
        dataFile.setHasTitle(request.getMetaSummary().getHasTitle());
        dataFile.setResourceName(request.getMetaSummary().getTableName());
        dataFile.setSize((long)request.getMetaSummary().getSize());
        dataFile.setRows((long)request.getMetaSummary().getRows());
        dataFile.setColumns(request.getMetaSummary().getColumns());
        dataFile.setRemarks(request.getMetaSummary().getDesc());
        dataFile.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        dataFile.setPublished(true);
        dataFile.setStatus(request.getMetaSummary().getState().ordinal());

        List<MetaDataColumn> metaDataColumnList = request.getColumnMetaList().stream().map(column -> {
            MetaDataColumn metaDataColumn = new MetaDataColumn();
            metaDataColumn.setMetaDataId(request.getMetaSummary().getMetadataId());
            metaDataColumn.setColumnIdx(column.getCIndex());
            metaDataColumn.setColumnName(column.getCName());
            metaDataColumn.setColumnType(column.getCType());
            metaDataColumn.setColumnSize(column.getCSize());
            metaDataColumn.setRemarks(column.getCComment());
            metaDataColumn.setPublished(true);
            return metaDataColumn;
        }).collect(Collectors.toList());

        metaDataService.insertMetaData(dataFile, metaDataColumnList);

        SimpleResponse response = SimpleResponse.newBuilder()
                .setStatus(0)
                .build();
        log.debug("metaDataSave, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 查看全部元数据摘要列表 (不包含 列字段描述)，状态为可用
     * </pre>
     */
    public void getMetadataSummaryList(com.google.protobuf.Empty request,
                                       io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.MetadataSummaryListResponse> responseObserver) {
        log.debug("getMetaDataSummaryList, request:{}", request);

        List<DataFile> dataFileList = metaDataService.listDataFile(MetadataState.MetadataState_Released.ordinal());

        List<MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryOwner(dataFileList);

        MetadataSummaryListResponse response = MetadataSummaryListResponse.newBuilder()
                .addAllMetadataSummaries(metaDataSummaryOwnerList)
                .build();
        log.debug("getMetaDataSummaryList, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增：元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    public void getMetadataList(com.platon.rosettanet.storage.grpc.lib.api.MetadataListRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.MetadataListResponse> responseObserver) {

        log.debug("getMetadataList, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<DataFile> dataFileList = metaDataService.syncDataFile(lastUpdateAt);

        List<MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryOwner(dataFileList);

        List<Metadata> metadataList = metaDataSummaryOwnerList.parallelStream().map(summaryOwner -> {
            String metaDataId = summaryOwner.getInformation().getMetadataId();

            List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
            List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.parallelStream().map(column ->{
                return this.convertorService.toProtoMetaDataColumnDetail(column);
            }).collect(Collectors.toList());

            return Metadata.newBuilder()
                    .setOwner(summaryOwner.getOwner())
                    .setMetaSummary(summaryOwner.getInformation())
                    .addAllMetadataColumns(metaDataColumnDetailList)
                    .build();

        }).collect(Collectors.toList());

        MetadataListResponse response = MetadataListResponse.newBuilder().addAllMetadatas(metadataList).build();

        log.debug("getMetadataList, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    public void getMetadataById(com.platon.rosettanet.storage.grpc.lib.api.MetadataByIdRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.MetadataByIdResponse> responseObserver) {

        log.debug("getMetadataById, request:{}", request);

        String metaDataId = request.getMetadataId();
        Metadata metadata = null;

        DataFile dataFile = metaDataService.findByMetaDataId(metaDataId);

        if(dataFile != null) {
            MetadataSummaryOwner summaryOwner = convertorService.toProtoMetaDataSummaryOwner(dataFile);

            List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
            List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.stream().map(column -> {
                return this.convertorService.toProtoMetaDataColumnDetail(column);
            }).collect(Collectors.toList());

            metadata = Metadata.newBuilder()
                    .setOwner(summaryOwner.getOwner())
                    .setMetaSummary(summaryOwner.getInformation())
                    .addAllMetadataColumns(metaDataColumnDetailList)
                    .build();
        }else{
            metadata = Metadata.getDefaultInstance();
        }

        MetadataByIdResponse response = MetadataByIdResponse.newBuilder().setMetadata(metadata).build();

        log.debug("getMetadataById, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 撤销元数据 (从底层网络撤销)
     * </pre>
     */
    public void revokeMetadata(com.platon.rosettanet.storage.grpc.lib.api.RevokeMetadataRequest request,
                               io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("revokeMetaData, request:{}", request);

        String metaDataId = request.getMetadataId();
        metaDataService.updateStatus(metaDataId, MetadataState.MetadataState_Revoked.ordinal());

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokeMetaData, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
