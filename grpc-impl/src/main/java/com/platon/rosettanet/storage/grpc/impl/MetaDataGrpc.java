package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.DataFile;
import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;
import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.MetadataState;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataPB;
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
    public void saveMetadata(com.platon.rosettanet.storage.grpc.lib.api.SaveMetadataRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("metaDataSave, request:{}", request);

        DataFile dataFile = new DataFile();
        dataFile.setOriginId(request.getMetadata().getOriginId());
        dataFile.setMetaDataId(request.getMetadata().getDataId());
        dataFile.setFileName(request.getMetadata().getTableName());
        dataFile.setFilePath(request.getMetadata().getFilePath());
        dataFile.setFileType(request.getMetadata().getFileType().name());
        dataFile.setIdentityId(request.getMetadata().getIdentityId());
        dataFile.setHasTitle(request.getMetadata().getHasTitle());
        dataFile.setResourceName(request.getMetadata().getTableName());
        dataFile.setSize((long)request.getMetadata().getSize());
        dataFile.setRows((long)request.getMetadata().getRows());
        dataFile.setColumns(request.getMetadata().getColumns());
        dataFile.setRemarks(request.getMetadata().getDesc());
        dataFile.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        dataFile.setPublished(true);
        dataFile.setStatus(request.getMetadata().getState().ordinal());

        List<MetaDataColumn> metaDataColumnList = request.getMetadata().getMetadataColumnsList().stream().map(column -> {
            MetaDataColumn metaDataColumn = new MetaDataColumn();
            metaDataColumn.setMetaDataId(request.getMetadata().getDataId());
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
    public void listMetadataSummary(com.google.protobuf.Empty request,
                                       io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.ListMetadataSummaryResponse> responseObserver) {
        log.debug("listMetadataSummary, request:{}", request);

        List<DataFile> dataFileList = metaDataService.listDataFile(MetadataState.MetadataState_Released.ordinal());

        List<MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryOwner(dataFileList);

        ListMetadataSummaryResponse response = ListMetadataSummaryResponse.newBuilder()
                .addAllMetadataSummaries(metaDataSummaryOwnerList)
                .build();
        log.debug("listMetadataSummary, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增：元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    public void listMetadata(com.platon.rosettanet.storage.grpc.lib.api.ListMetadataRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.ListMetadataResponse> responseObserver) {

        log.debug("listMetadata, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<DataFile> dataFileList = metaDataService.syncDataFile(lastUpdateAt);

        List<MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);

        List<MetadataPB> metadataList = mtadataPBList.parallelStream().map(metadataPB -> {
            String metaDataId = metadataPB.getMetadataId();

            List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
            List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.parallelStream().map(column ->{
                return this.convertorService.toProtoMetaDataColumnDetail(column);
            }).collect(Collectors.toList());

            return MetadataPB.newBuilder()
                    .setIdentityId(metadataPB.getIdentityId())
                    .addAllMetadataColumns(metaDataColumnDetailList)
                    .build();

        }).collect(Collectors.toList());

        ListMetadataResponse response = ListMetadataResponse.newBuilder().addAllMetadata(metadataList).build();

        log.debug("listMetadata, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    public void findMetadataById(com.platon.rosettanet.storage.grpc.lib.api.FindMetadataByIdRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.FindMetadataByIdResponse> responseObserver) {

        log.debug("findMetadataById, request:{}", request);

        String metaDataId = request.getMetadataId();
        MetadataPB metadataPB = null;

        DataFile dataFile = metaDataService.findByMetaDataId(metaDataId);

        if(dataFile != null) {
            metadataPB = convertorService.toProtoMetadataPB(dataFile);
        }else{
            metadataPB = MetadataPB.getDefaultInstance();
        }

        FindMetadataByIdResponse response = FindMetadataByIdResponse.newBuilder().setMetadata(metadataPB).build();

        log.debug("findMetadataById, response:{}", response);
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
