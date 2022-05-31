package com.platon.datum.storage.grpc.impl;

import com.platon.datum.storage.common.exception.MetaDataNotFound;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.MetaData;
import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.Metadata;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Metadata.*;
import com.platon.datum.storage.grpc.datacenter.api.MetadataServiceGrpc;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.MetaDataService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
    @Override
    public void saveMetadata(SaveMetadataRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        log.debug("metaDataSave, request:{}", request);
        Metadata.MetadataPB metadata = request.getMetadata();
        MetaData dataFile = new MetaData();
        dataFile.setMetaDataId(metadata.getMetadataId());
        dataFile.setIdentityId(metadata.getOwner().getIdentityId());
        dataFile.setDataId(metadata.getDataId());
        dataFile.setDataStatus(metadata.getDataStatus().getNumber());
        dataFile.setMetaDataName(metadata.getMetadataName());
        dataFile.setMetaDataType(metadata.getMetadataType().getNumber());
        dataFile.setDataHash(metadata.getDataHash());
        dataFile.setDesc(metadata.getDesc());
        dataFile.setLocationType(metadata.getLocationType().getNumber());
        dataFile.setDataType(metadata.getDataType().getNumber());
        dataFile.setIndustry(metadata.getIndustry());
        dataFile.setStatus(metadata.getState().getNumber());
        dataFile.setPublishAt(LocalDateTime.now(ZoneOffset.UTC));
        dataFile.setUpdateAt(LocalDateTime.now(ZoneOffset.UTC));
        dataFile.setNonce(metadata.getNonce());
        dataFile.setMetaDataOption(metadata.getMetadataOption());
        dataFile.setAllowExpose(metadata.getAllowExpose() ? 1 : 0);
        dataFile.setTokenAddress(metadata.getTokenAddress());
        metaDataService.insertMetaData(dataFile);
        Common.SimpleResponse response = Common.SimpleResponse.newBuilder()
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
    @Override
    public void listMetadataSummary(ListMetadataSummaryRequest request,
                                    io.grpc.stub.StreamObserver<ListMetadataSummaryResponse> responseObserver) {
        log.debug("listMetadataSummary, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<MetaData> dataFileList = metaDataService.listDataFile(CarrierEnum.MetadataState.MetadataState_Released.ordinal(), lastUpdateAt, request.getPageSize());

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
    @Override
    public void listMetadata(ListMetadataRequest request,
                             io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {

        log.debug("listMetadata, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFile(lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        ListMetadataResponse response;
        if (CollectionUtils.isEmpty(dataFileList)) {
            response = ListMetadataResponse.newBuilder().build();
        } else {
            List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
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
    @Override
    public void listMetadataByIdentityId(ListMetadataByIdentityIdRequest request,
                                         io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {
        log.debug("listMetadataByIdentityId, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFileByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        ListMetadataResponse response;
        if (CollectionUtils.isEmpty(dataFileList)) {
            response = ListMetadataResponse.newBuilder().build();
        } else {
            List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
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
    @Override
    public void findMetadataById(FindMetadataByIdRequest request,
                                 io.grpc.stub.StreamObserver<FindMetadataByIdResponse> responseObserver) {

        log.debug("findMetadataById, request:{}", request);

        String metaDataId = request.getMetadataId();
        Metadata.MetadataPB metadataPB = null;

        //1.查询元数据信息
        MetaData dataFile = metaDataService.findByMetaDataId(metaDataId);

        //2.将元数据信息转换成proto接口所需的数据结构
        if (dataFile != null) {
            metadataPB = convertorService.toProtoMetadataPB(dataFile);
        } else {
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
     * 新增，根据多个元数据ID查询多个元数据详情
     * </pre>
     */
    @Override
    public void findMetadataByIds(FindMetadataByIdsRequest request,
                                  io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {

        log.debug("findMetadataByIds, request:{}", request);

        List<String> metaDataIdList = request.getMetadataIdsList();
        //1.查询元数据信息
        List<MetaData> dataFileList = metaDataService.findByMetaDataIdList(metaDataIdList);

        ListMetadataResponse response = null;
        //2.将元数据信息转换成proto接口所需的数据结构
        if (CollectionUtils.isEmpty(dataFileList)) {
            response = ListMetadataResponse.newBuilder().build();
        } else {
            List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
            response = ListMetadataResponse.newBuilder().addAllMetadata(mtadataPBList).build();
        }
        log.debug("findMetadataByIds, response:{}", response);
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
    @Override
    public void revokeMetadata(RevokeMetadataRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        log.debug("revokeMetaData, request:{}", request);

        String metaDataId = request.getMetadataId();
        metaDataService.updateStatus(metaDataId, CarrierEnum.MetadataState.MetadataState_Revoked.ordinal());

        Common.SimpleResponse response = Common.SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokeMetaData, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 更新已经发布的元数据信息 v 0.4.0 绑定合约地址
     * </pre>
     */
    @Transactional
    @Override
    public void updateMetadata(UpdateMetadataRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        log.debug("updateMetadata, request:{}", request);

        Metadata.MetadataPB metadata = request.getMetadata();
        MetaData dataFile = new MetaData();
        dataFile.setMetaDataId(metadata.getMetadataId());
        dataFile.setIdentityId(metadata.getOwner().getIdentityId());
        dataFile.setDataId(metadata.getDataId());
        dataFile.setDataStatus(metadata.getDataStatus().getNumber());
        dataFile.setMetaDataName(metadata.getMetadataName());
        dataFile.setMetaDataType(metadata.getMetadataType().getNumber());
        dataFile.setDataHash(metadata.getDataHash());
        dataFile.setDesc(metadata.getDesc());
        dataFile.setLocationType(metadata.getLocationType().getNumber());
        dataFile.setDataType(metadata.getDataType().getNumber());
        dataFile.setIndustry(metadata.getIndustry());
        dataFile.setStatus(metadata.getState().getNumber());
        dataFile.setNonce(metadata.getNonce());
        dataFile.setMetaDataOption(metadata.getMetadataOption());
        dataFile.setAllowExpose(metadata.getAllowExpose() ? 1 : 0);
        dataFile.setTokenAddress(metadata.getTokenAddress());

        metaDataService.update(dataFile);

        Common.SimpleResponse response = Common.SimpleResponse.newBuilder().setStatus(0).build();

        log.debug("revokeMetaData, response:{}", response);

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
