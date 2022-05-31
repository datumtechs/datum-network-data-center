package com.platon.datum.storage.service;

import com.platon.datum.storage.dao.entity.*;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.TaskData;
import com.platon.datum.storage.grpc.datacenter.api.Metadata;

import java.util.List;


/**
 * 该类负责将数据库数据转换成proto接口所需数据结构
 */
public interface ConvertorService {

    IdentityData.IdentityPB toProtoIdentityPB(OrgInfo orgInfo);

    IdentityData.Organization toProtoOrganization(OrgInfo orgInfo);

    List<com.platon.datum.storage.grpc.carrier.types.TaskData.TaskEvent> toProtoTaskEvent(List<TaskEvent> taskEventList);

    com.platon.datum.storage.grpc.carrier.types.TaskData.TaskEvent toProtoTaskEvent(TaskEvent taskEvent);

    Metadata.MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile);

    List<Metadata.MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList);

    com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataPB toProtoMetadataPB(MetaData dataFile);

    List<com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList);

    TaskData.TaskPB toTaskPB(TaskInfo taskInfo);

    List<TaskData.TaskPB> toTaskPB(List<TaskInfo> taskInfoList);

    com.platon.datum.storage.grpc.carrier.types.Metadata.MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth);
}
