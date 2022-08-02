package com.platon.datum.storage.grpc.impl;

import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.MetaData;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.Metadata;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Metadata.*;
import com.platon.datum.storage.grpc.datacenter.api.MetadataServiceGrpc;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.MetaDataService;
import com.platon.datum.storage.service.OrgInfoService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@GrpcService
@Service
public class MetaDataGrpc extends MetadataServiceGrpc.MetadataServiceImplBase {

    @Resource
    private MetaDataService metaDataService;

    @Resource
    private ConvertorService convertorService;

    @Resource
    private OrgInfoService orgInfoService;

    /**
     * <pre>
     * 保存元数据
     * </pre>
     */
    @Transactional
    @Override
    public void saveMetadata(SaveMetadataRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveMetadataInternal(input),
                "saveMetadata");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void saveMetadataInternal(SaveMetadataRequest request){
        Metadata.MetadataPB metadata = request.getMetadata();

        com.platon.datum.storage.grpc.carrier.types.IdentityData.Organization owner = metadata.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }

        MetaData dataFile = new MetaData();
        dataFile.setMetaDataId(metadata.getMetadataId());
        dataFile.setIdentityId(owner.getIdentityId());
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
        dataFile.setUser(metadata.getUser());
        dataFile.setUserType(metadata.getUserTypeValue());
        dataFile.setSign(metadata.getSign().toStringUtf8());
        metaDataService.insertMetaData(dataFile);
    }

    /**
     * <pre>
     * 查看全部元数据摘要列表 (不包含 列字段描述)，状态为可用
     * </pre>
     */
    @Override
    public void listMetadataSummary(ListMetadataSummaryRequest request,
                                    io.grpc.stub.StreamObserver<ListMetadataSummaryResponse> responseObserver) {
        ListMetadataSummaryResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataSummaryInternal(input),
                bizOut -> ListMetadataSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadataSummaries(bizOut).build(),
                bizError -> ListMetadataSummaryResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> ListMetadataSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadataSummary"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<MetadataSummaryOwner> listMetadataSummaryInternal(ListMetadataSummaryRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<MetaData> dataFileList = metaDataService.listDataFile(CarrierEnum.MetadataState.MetadataState_Released.ordinal(), lastUpdateAt, request.getPageSize());

        List<MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryWithOwner(dataFileList);
        return metaDataSummaryOwnerList;
    }

    /**
     * <pre>
     * 新增：元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    @Override
    public void listMetadata(ListMetadataRequest request,
                             io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {
        ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataInternal(input),
                bizOut -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadata"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Metadata.MetadataPB> listMetadataInternal(ListMetadataRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFile(lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
        return mtadataPBList;
    }


    /**
     * <pre>
     * 新增：对应identityId的元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    @Override
    public void listMetadataByIdentityId(ListMetadataByIdentityIdRequest request,
                                         io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {
        ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataByIdentityIdInternal(input),
                bizOut -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadataByIdentityId"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Metadata.MetadataPB> listMetadataByIdentityIdInternal(ListMetadataByIdentityIdRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFileByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);

        return mtadataPBList;
    }


    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    @Override
    public void findMetadataById(FindMetadataByIdRequest request,
                                 io.grpc.stub.StreamObserver<FindMetadataByIdResponse> responseObserver) {
        FindMetadataByIdResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataByIdInternal(input),
                bizOut -> FindMetadataByIdResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setMetadata(bizOut).build(),
                bizError -> FindMetadataByIdResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> FindMetadataByIdResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataById"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Metadata.MetadataPB findMetadataByIdInternal(FindMetadataByIdRequest request) {
        String metaDataId = request.getMetadataId();
        Metadata.MetadataPB metadataPB = null;

        //1.查询元数据信息
        MetaData dataFile = metaDataService.findByMetaDataId(metaDataId);

        //2.将元数据信息转换成proto接口所需的数据结构
        if (dataFile != null) {
            metadataPB = convertorService.toProtoMetadataPB(dataFile);
            return metadataPB;
        } else {
            throw new BizException(CodeEnums.METADATA_NOT_FOUND);
        }
    }

    /**
     * <pre>
     * 新增，根据多个元数据ID查询多个元数据详情
     * </pre>
     */
    @Override
    public void findMetadataByIds(FindMetadataByIdsRequest request,
                                  io.grpc.stub.StreamObserver<ListMetadataResponse> responseObserver) {
        ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataByIdsInternal(input),
                bizOut -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataByIds"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Metadata.MetadataPB> findMetadataByIdsInternal(FindMetadataByIdsRequest request) {
        List<String> metaDataIdList = request.getMetadataIdsList();
        //1.查询元数据信息
        List<MetaData> dataFileList = metaDataService.findByMetaDataIdList(metaDataIdList);
        //2.将元数据信息转换成proto接口所需的数据结构
        List<Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
        return mtadataPBList;
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
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> revokeMetadata(input),
                "revokeMetaData");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void revokeMetadata(RevokeMetadataRequest request) {
        com.platon.datum.storage.grpc.carrier.types.IdentityData.Organization owner = request.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }

        String metaDataId = request.getMetadataId();
        metaDataService.updateStatus(metaDataId, CarrierEnum.MetadataState.MetadataState_Revoked.ordinal());
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

        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateMetadata(input),
                "updateMetadata");

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void updateMetadata(UpdateMetadataRequest request) {

        Metadata.MetadataPB metadata = request.getMetadata();

        com.platon.datum.storage.grpc.carrier.types.IdentityData.Organization owner = metadata.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }

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
        dataFile.setUser(metadata.getUser());
        dataFile.setUserType(metadata.getUserTypeValue());
        dataFile.setSign(metadata.getSign().toStringUtf8());

        metaDataService.update(dataFile);
    }
}
