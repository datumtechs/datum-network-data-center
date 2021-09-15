package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataAuthorityPB;
import com.platon.rosettanet.storage.grpc.lib.types.TaskDataSupplier;

import java.util.List;


public interface ConvertorService {
    List<TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList);

    List<com.platon.rosettanet.storage.grpc.lib.types.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList);

    List<com.platon.rosettanet.storage.grpc.lib.common.TaskOrganization> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList);

    com.platon.rosettanet.storage.grpc.lib.common.Organization toProtoOrganization(OrgInfo orgInfo);

    com.platon.rosettanet.storage.grpc.lib.common.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId);

    List<com.platon.rosettanet.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.rosettanet.storage.dao.entity.TaskEvent> taskEventList);

    com.platon.rosettanet.storage.grpc.lib.types.TaskEvent toProtoTaskEvent(com.platon.rosettanet.storage.dao.entity.TaskEvent taskEvent);

    com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn toProtoMetaDataColumnDetail(MetaDataColumn metaDataColumn);

    com.platon.rosettanet.storage.grpc.lib.api.MetadataSummaryOwner toProtoMetaDataSummaryOwner(DataFile dataFile);

    List<MetadataSummaryOwner> toProtoMetaDataSummaryOwner(List<DataFile> dataFileList);


    com.platon.rosettanet.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(DataFile dataFile);

    List<com.platon.rosettanet.storage.grpc.lib.types.MetadataPB> toProtoMetadataPB(List<DataFile> dataFileList);

    MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth);

    com.platon.rosettanet.storage.grpc.lib.types.TaskPB toTaskPB(Task task);
    List<com.platon.rosettanet.storage.grpc.lib.types.TaskPB> toTaskPB(List<Task> taskList);
}
