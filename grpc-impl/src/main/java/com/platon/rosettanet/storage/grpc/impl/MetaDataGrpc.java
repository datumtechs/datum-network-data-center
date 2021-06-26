package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.DataFile;
import com.platon.rosettanet.storage.dao.entity.MetaDataColumn;
import com.platon.rosettanet.storage.grpc.lib.*;
import com.platon.rosettanet.storage.service.ConvertorService;
import com.platon.rosettanet.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class MetaDataGrpc extends MetaDataServiceGrpc.MetaDataServiceImplBase {

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private ConvertorService convertorService;

    /**
     * <pre>
     * 保存元数据
     * </pre>
     */
    public void metaDataSave(com.platon.rosettanet.storage.grpc.lib.MetaDataSaveRequest request,
                             io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.SimpleResponse> responseObserver) {

        DataFile dataFile = new DataFile();
        dataFile.setId(request.getMetaSummary().getOriginId());
        dataFile.setMetaDataId(request.getMetaSummary().getMetaDataId());
        dataFile.setFileName(request.getMetaSummary().getTableName());
        dataFile.setFilePath(request.getMetaSummary().getFilePath());
        dataFile.setFileType(request.getMetaSummary().getFileType());
        dataFile.setIdentityId(request.getOwner().getIdentityId());
        dataFile.setHasTitle(request.getMetaSummary().getHasTitle());
        dataFile.setResourceName(request.getMetaSummary().getTableName());
        dataFile.setSize(request.getMetaSummary().getSize());
        dataFile.setRows((long)request.getMetaSummary().getRows());
        dataFile.setColumns(request.getMetaSummary().getColumns());
        dataFile.setRemarks(request.getMetaSummary().getDesc());
        dataFile.setPublishedAt(LocalDateTime.now());
        dataFile.setPublished(true);
        dataFile.setStatus(request.getMetaSummary().getState());

        List<MetaDataColumn> metaDataColumnList = request.getColumnMetaList().stream().map(column -> {
            MetaDataColumn metaDataColumn = new MetaDataColumn();
            metaDataColumn.setMetaDataId(request.getMetaSummary().getOriginId());
            metaDataColumn.setColumnIdx(column.getCindex());
            metaDataColumn.setColumnName(column.getCname());
            metaDataColumn.setColumnType(column.getCtype());
            metaDataColumn.setRemarks(column.getCcomment());
            metaDataColumn.setPublished(true);
            return metaDataColumn;
        }).collect(Collectors.toList());

        metaDataService.insertMetaData(dataFile, metaDataColumnList);

        SimpleResponse response = SimpleResponse.newBuilder()
                .setStatus(0)
                .build();

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 查看全部元数据摘要列表 (不包含 列字段描述)，状态为可用
     * </pre>
     */
    public void getMetaDataSummaryList(com.google.protobuf.Empty request,
                                       io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.MetaDataSummaryListResponse> responseObserver) {

        List<DataFile> dataFileList = metaDataService.listDataFile("released");

        List<MetaDataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryOwner(dataFileList);

        MetaDataSummaryListResponse response = MetaDataSummaryListResponse.newBuilder()
                .addAllMetadataSummaryList(metaDataSummaryOwnerList)
                .build();

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增：元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    public void getMetadataList(com.platon.rosettanet.storage.grpc.lib.MetadataListRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.MetadataListResponse> responseObserver) {
        List<DataFile> dataFileList = metaDataService.listDataFile("released");


        List<MetaDataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryOwner(dataFileList);

        List<Metadata> metadataList = metaDataSummaryOwnerList.stream().map(summaryOwner -> {
            String metaDataId = summaryOwner.getInformation().getMetaDataId();

            List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
            List<MetaDataColumnDetail> metaDataColumnDetailList = metaDataColumnList.stream().map(column ->{
                return this.convertorService.toProtoMetaDataColumnDetail(column);
            }).collect(Collectors.toList());

            return Metadata.newBuilder()
                    .setOwner(summaryOwner.getOwner())
                    .setMetaSummary(summaryOwner.getInformation())
                    .addAllColumnMeta(metaDataColumnDetailList)
                    .build();

        }).collect(Collectors.toList());

        MetadataListResponse response = MetadataListResponse.newBuilder().addAllMetadataList(metadataList).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    public void getMetadataById(com.platon.rosettanet.storage.grpc.lib.MetadataByIdRequest request,
                                io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.MetadataByIdResponse> responseObserver) {

        String metaDataId = request.getMetadataId();
        Metadata metadata = null;

        DataFile dataFile = metaDataService.findByMetaDataId(metaDataId);

        if(dataFile != null) {
            MetaDataSummaryOwner summaryOwner = convertorService.toProtoMetaDataSummaryOwner(dataFile);

            List<MetaDataColumn> metaDataColumnList = metaDataService.listMetaDataColumn(metaDataId);
            List<MetaDataColumnDetail> metaDataColumnDetailList = metaDataColumnList.stream().map(column -> {
                return this.convertorService.toProtoMetaDataColumnDetail(column);
            }).collect(Collectors.toList());

            metadata = Metadata.newBuilder()
                    .setOwner(summaryOwner.getOwner())
                    .setMetaSummary(summaryOwner.getInformation())
                    .addAllColumnMeta(metaDataColumnDetailList)
                    .build();
        }else{
            metadata = Metadata.getDefaultInstance();
        }

        MetadataByIdResponse response = MetadataByIdResponse.newBuilder().setMetadata(metadata).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 撤销元数据 (从底层网络撤销)
     * </pre>
     */
    public void revokeMetaData(com.platon.rosettanet.storage.grpc.lib.RevokeMetaDataRequest request,
                               io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.SimpleResponse> responseObserver) {
        String metaDataId = request.getMetaDataId();
        metaDataService.deleteByMetaDataId(metaDataId);

        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
