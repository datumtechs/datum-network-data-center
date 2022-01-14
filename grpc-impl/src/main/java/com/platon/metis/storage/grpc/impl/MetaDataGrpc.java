package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.common.exception.MetaDataNotFound;
import com.platon.metis.storage.dao.entity.DataFile;
import com.platon.metis.storage.dao.entity.MetaDataColumn;
import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.common.MetadataState;
import com.platon.metis.storage.grpc.lib.common.SimpleResponse;
import com.platon.metis.storage.grpc.lib.types.MetadataColumn;
import com.platon.metis.storage.grpc.lib.types.MetadataPB;
import com.platon.metis.storage.service.ConvertorService;
import com.platon.metis.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    @Transactional
    public void saveMetadata(com.platon.metis.storage.grpc.lib.api.SaveMetadataRequest request,
                             io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {

        log.debug("metaDataSave, request:{}", request);

        DataFile dataFile = new DataFile();
        dataFile.setOriginId(request.getMetadata().getOriginId());
        dataFile.setMetaDataId(request.getMetadata().getDataId());
        dataFile.setFileName(request.getMetadata().getTableName());
        dataFile.setFilePath(request.getMetadata().getFilePath());
        dataFile.setFileType(request.getMetadata().getFileType().getNumber());
        dataFile.setIdentityId(request.getMetadata().getIdentityId());
        dataFile.setHasTitle(request.getMetadata().getHasTitle());
        dataFile.setResourceName(request.getMetadata().getTableName());
        dataFile.setSize(request.getMetadata().getSize());
        dataFile.setRows(request.getMetadata().getRows());
        dataFile.setColumns(request.getMetadata().getColumns());
        dataFile.setRemarks(request.getMetadata().getDesc());
        dataFile.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        dataFile.setPublished(true);
        dataFile.setStatus(request.getMetadata().getState().ordinal());
        dataFile.setIndustry(request.getMetadata().getIndustry());
        dataFile.setDfsDataId(request.getMetadata().getDataId());
        dataFile.setDfsDataStatus(request.getMetadata().getDataStatus().getNumber());


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
    public void listMetadataSummary(ListMetadataSummaryRequest request,
                                       io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListMetadataSummaryResponse> responseObserver) {
        log.debug("listMetadataSummary, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<DataFile> dataFileList = metaDataService.listDataFile(MetadataState.MetadataState_Released.ordinal(), lastUpdateAt, request.getPageSize());

        List<MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryWithOwner(dataFileList);

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
    public void listMetadata(com.platon.metis.storage.grpc.lib.api.ListMetadataRequest request,
                                io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListMetadataResponse> responseObserver) {

        log.debug("listMetadata, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<DataFile> dataFileList = metaDataService.syncDataFile(lastUpdateAt, request.getPageSize());

        ListMetadataResponse response;

        if(CollectionUtils.isEmpty(dataFileList)) {
            response = ListMetadataResponse.newBuilder().build();
        }else{

            List<MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);

             mtadataPBList.parallelStream().forEach(metadataPB -> {
                String metaDataId = metadataPB.getMetadataId();

                List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
                List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.parallelStream().map(column ->{
                    return this.convertorService.toProtoMetaDataColumnDetail(column);
                }).collect(Collectors.toList());

                metadataPB.toBuilder().addAllMetadataColumns(metaDataColumnDetailList);

            });
            response = ListMetadataResponse.newBuilder().addAllMetadata(mtadataPBList).build();
        }

        log.debug("listMetadata, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 新增：对应identityId的元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    public void listMetadataByIdentityId(com.platon.metis.storage.grpc.lib.api.ListMetadataByIdentityIdRequest request,
                                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListMetadataResponse> responseObserver) {
        log.debug("listMetadataByIdentityId, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<DataFile> dataFileList = metaDataService.syncDataFileByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        ListMetadataResponse response;

        if(CollectionUtils.isEmpty(dataFileList)) {
            response = ListMetadataResponse.newBuilder().build();
        }else{

            List<MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);

            mtadataPBList.parallelStream().forEach(metadataPB -> {
                String metaDataId = metadataPB.getMetadataId();

                List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
                List<MetadataColumn> metaDataColumnDetailList = metaDataColumnList.parallelStream().map(column ->{
                    return this.convertorService.toProtoMetaDataColumnDetail(column);
                }).collect(Collectors.toList());

                metadataPB.toBuilder().addAllMetadataColumns(metaDataColumnDetailList);

            });
            response = ListMetadataResponse.newBuilder().addAllMetadata(mtadataPBList).build();
        }

        log.debug("listMetadataByIdentityId, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    public void findMetadataById(com.platon.metis.storage.grpc.lib.api.FindMetadataByIdRequest request,
                                io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.FindMetadataByIdResponse> responseObserver) {

        log.debug("findMetadataById, request:{}", request);

        String metaDataId = request.getMetadataId();
        MetadataPB metadataPB = null;

        DataFile dataFile = metaDataService.findByMetaDataId(metaDataId);

        if(dataFile != null) {
            metadataPB = convertorService.toProtoMetadataPB(dataFile);
        }else{
            throw new MetaDataNotFound();
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
    @Transactional
    public void revokeMetadata(com.platon.metis.storage.grpc.lib.api.RevokeMetadataRequest request,
                               io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.common.SimpleResponse> responseObserver) {

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
