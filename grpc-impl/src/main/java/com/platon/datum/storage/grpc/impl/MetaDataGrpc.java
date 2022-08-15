package com.platon.datum.storage.grpc.impl;

import carrier.types.Common;
import carrier.types.Identitydata;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.exception.BizException;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.MetaData;
import com.platon.datum.storage.dao.entity.OrgInfo;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.ConvertorService;
import com.platon.datum.storage.service.MetaDataService;
import com.platon.datum.storage.service.OrgInfoService;
import common.constant.CarrierEnum;
import datacenter.api.Metadata;
import datacenter.api.MetadataServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Override
    public void saveMetadata(Metadata.SaveMetadataRequest request,
                             io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveMetadataInternal(input),
                "saveMetadata");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void saveMetadataInternal(Metadata.SaveMetadataRequest request){
        carrier.types.Metadata.MetadataPB metadata = request.getMetadata();

        Identitydata.Organization owner = metadata.getOwner();
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
    public void listMetadataSummary(Metadata.ListMetadataSummaryRequest request,
                                    io.grpc.stub.StreamObserver<Metadata.ListMetadataSummaryResponse> responseObserver) {
        Metadata.ListMetadataSummaryResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataSummaryInternal(input),
                bizOut -> Metadata.ListMetadataSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadataSummaries(bizOut).build(),
                bizError -> Metadata.ListMetadataSummaryResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Metadata.ListMetadataSummaryResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadataSummary"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Metadata.MetadataSummaryOwner> listMetadataSummaryInternal(Metadata.ListMetadataSummaryRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<MetaData> dataFileList = metaDataService.listDataFile(CarrierEnum.MetadataState.MetadataState_Released.ordinal(), lastUpdateAt, request.getPageSize());

        List<Metadata.MetadataSummaryOwner> metaDataSummaryOwnerList = convertorService.toProtoMetaDataSummaryWithOwner(dataFileList);
        return metaDataSummaryOwnerList;
    }

    /**
     * <pre>
     * 新增：元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    @Override
    public void listMetadata(Metadata.ListMetadataRequest request,
                             io.grpc.stub.StreamObserver<Metadata.ListMetadataResponse> responseObserver) {
        Metadata.ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataInternal(input),
                bizOut -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadata"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<carrier.types.Metadata.MetadataPB> listMetadataInternal(Metadata.ListMetadataRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFile(lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        List<carrier.types.Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
        return mtadataPBList;
    }


    /**
     * <pre>
     * 新增：对应identityId的元数据详细列表（用于将数据同步给管理台，考虑checkpoint同步点位）
     * </pre>
     */
    @Override
    public void listMetadataByIdentityId(Metadata.ListMetadataByIdentityIdRequest request,
                                         io.grpc.stub.StreamObserver<Metadata.ListMetadataResponse> responseObserver) {
        Metadata.ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> listMetadataByIdentityIdInternal(input),
                bizOut -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listMetadataByIdentityId"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<carrier.types.Metadata.MetadataPB> listMetadataByIdentityIdInternal(Metadata.ListMetadataByIdentityIdRequest request) {
        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        //1.从数据库中查询出元数据信息
        List<MetaData> dataFileList = metaDataService.syncDataFileByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        //2.将元数据信息转换成proto接口所需的数据结构
        List<carrier.types.Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);

        return mtadataPBList;
    }


    /**
     * <pre>
     * 新增，根据元数据ID查询元数据详情
     * </pre>
     */
    @Override
    public void findMetadataById(Metadata.FindMetadataByIdRequest request,
                                 io.grpc.stub.StreamObserver<Metadata.FindMetadataByIdResponse> responseObserver) {
        Metadata.FindMetadataByIdResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataByIdInternal(input),
                bizOut -> Metadata.FindMetadataByIdResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setMetadata(bizOut).build(),
                bizError -> Metadata.FindMetadataByIdResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Metadata.FindMetadataByIdResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataById"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private carrier.types.Metadata.MetadataPB findMetadataByIdInternal(Metadata.FindMetadataByIdRequest request) {
        String metaDataId = request.getMetadataId();
        carrier.types.Metadata.MetadataPB metadataPB = null;

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
    public void findMetadataByIds(Metadata.FindMetadataByIdsRequest request,
                                  io.grpc.stub.StreamObserver<Metadata.ListMetadataResponse> responseObserver) {
        Metadata.ListMetadataResponse response = GrpcImplUtils.query(
                request,
                input -> findMetadataByIdsInternal(input),
                bizOut -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllMetadata(bizOut).build(),
                bizError -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Metadata.ListMetadataResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"findMetadataByIds"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<carrier.types.Metadata.MetadataPB> findMetadataByIdsInternal(Metadata.FindMetadataByIdsRequest request) {
        List<String> metaDataIdList = request.getMetadataIdsList();
        //1.查询元数据信息
        List<MetaData> dataFileList = metaDataService.findByMetaDataIdList(metaDataIdList);
        //2.将元数据信息转换成proto接口所需的数据结构
        List<carrier.types.Metadata.MetadataPB> mtadataPBList = convertorService.toProtoMetadataPB(dataFileList);
        return mtadataPBList;
    }

    /**
     * <pre>
     * 撤销元数据 (从底层网络撤销)
     * </pre>
     */
    @Override
    public void revokeMetadata(Metadata.RevokeMetadataRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> revokeMetadata(input),
                "revokeMetaData");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void revokeMetadata(Metadata.RevokeMetadataRequest request) {
        Identitydata.Organization owner = request.getOwner();
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

    @Override
    public void updateMetadata(Metadata.UpdateMetadataRequest request,
                               io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {

        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> updateMetadata(input),
                    "updateMetadata");

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public void updateMetadata(Metadata.UpdateMetadataRequest request) {

        carrier.types.Metadata.MetadataPB metadata = request.getMetadata();

        Identitydata.Organization owner = metadata.getOwner();
        OrgInfo orgInfo = orgInfoService.findByPK(owner.getIdentityId());
        if (orgInfo == null) {
            log.error("identity not found. identityId:={}", owner.getIdentityId());
            throw new BizException(CodeEnums.ORG_NOT_FOUND);
        }

        Map<Integer, Optional<String>> req = getAddressFromOption(metadata.getMetadataOption());
        if(req.get(2).isPresent() || req.get(3).isPresent()){
            MetaData dbMetaData = metaDataService.findByMetaDataId(metadata.getMetadataId());
            Map<Integer, Optional<String>> db = getAddressFromOption(dbMetaData.getMetaDataOption());
            if(req.get(2).isPresent() && db.get(2).isPresent() && !StringUtils.equals(req.get(2).get(), db.get(2).get())){
                throw new BizException(CodeEnums.METADATA_CONTRACT_HAVE_SET);
            }
            if(req.get(3).isPresent() && db.get(3).isPresent() && !StringUtils.equals(req.get(3).get(), db.get(3).get())){
                throw new BizException(CodeEnums.METADATA_CONTRACT_HAVE_SET);
            }
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

    private static Map<Integer, Optional<String>>  getAddressFromOption(String option){
        JSONObject optionObj = JSONObject.parseObject(option);
        Map<Integer,  Optional<String>> result = new HashMap<>();
        result.put(2, Optional.empty());
        result.put(3, Optional.empty());
        JSONArray consumeTypes = optionObj.getJSONArray("consumeTypes");
        JSONArray consumeOptions = optionObj.getJSONArray("consumeOptions");
        for (int i = 0; i < consumeTypes.size(); i++) {
            // 无属性
            if(consumeTypes.getIntValue(i) == 2){
                result.put(2, Optional.of(JSONArray.parseArray(consumeOptions.getString(i)).getJSONObject(0).getString("contract")));
            }
            //  有属性
            if(consumeTypes.getIntValue(i) == 3) {
                result.put(3, Optional.of(JSONArray.parseArray(consumeOptions.getString(i)).getString(0)));
            }
        }
        return result;
    }
}
