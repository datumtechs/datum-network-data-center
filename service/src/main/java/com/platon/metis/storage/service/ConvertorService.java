package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.MetaData;
import com.platon.metis.storage.dao.entity.OrgInfo;
import com.platon.metis.storage.dao.entity.TaskResultConsumer;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;

import java.util.List;


/**
 * 该类负责将数据库数据转换成proto接口所需数据结构
 */
public interface ConvertorService {
//    List<TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList);
//
//    List<com.platon.metis.storage.grpc.lib.types.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList);

    List<com.platon.metis.storage.grpc.lib.types.Base.TaskOrganization> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList);

    com.platon.metis.storage.grpc.lib.types.Base.Organization toProtoOrganization(OrgInfo orgInfo);

    com.platon.metis.storage.grpc.lib.types.Base.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId);

    List<com.platon.metis.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.metis.storage.dao.entity.TaskEvent> taskEventList);

    com.platon.metis.storage.grpc.lib.types.TaskEvent toProtoTaskEvent(com.platon.metis.storage.dao.entity.TaskEvent taskEvent);

    com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile);

    List<MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList);


    com.platon.metis.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(MetaData dataFile);

    List<com.platon.metis.storage.grpc.lib.types.MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList);

//    MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth);

//    com.platon.metis.storage.grpc.lib.types.TaskPB toTaskPB(Task task);
//    List<com.platon.metis.storage.grpc.lib.types.TaskPB> toTaskPB(List<Task> taskList);
}
