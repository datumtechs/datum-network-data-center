package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.MetaDataSummaryOwner;

import java.util.List;


public interface ConvertorService {
    List<com.platon.rosettanet.storage.grpc.lib.TaskDataSupplier> toProtoDataSupplier(List<TaskMetaData> taskMetaDataList);

    List<com.platon.rosettanet.storage.grpc.lib.TaskPowerSupplier> toProtoPowerSupplier(List<TaskPowerProvider> taskPowerProviderList);

    List<com.platon.rosettanet.storage.grpc.lib.TaskResultReceiver> toProtoResultReceiver(List<TaskResultConsumer> taskResultConsumerList);

    com.platon.rosettanet.storage.grpc.lib.Organization toProtoOrganization(OrgInfo orgInfo);

    com.platon.rosettanet.storage.grpc.lib.TaskOrganization toProtoTaskOrganization(OrgInfo orgInfo, String partyId);

    List<com.platon.rosettanet.storage.grpc.lib.TaskEvent> toProtoTaskEvent(List<com.platon.rosettanet.storage.dao.entity.TaskEvent> taskEventList);

    com.platon.rosettanet.storage.grpc.lib.TaskEvent toProtoTaskEvent(com.platon.rosettanet.storage.dao.entity.TaskEvent taskEvent);

    com.platon.rosettanet.storage.grpc.lib.MetaDataColumnDetail toProtoMetaDataColumnDetail(MetaDataColumn metaDataColumn);

    com.platon.rosettanet.storage.grpc.lib.MetaDataSummaryOwner toProtoMetaDataSummaryOwner(DataFile dataFile);

    List<MetaDataSummaryOwner> toProtoMetaDataSummaryOwner(List<DataFile> dataFileList);
}
