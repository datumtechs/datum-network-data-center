package com.platon.metis.storage.service;

import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner;
import com.platon.metis.storage.grpc.lib.types.Base;
import com.platon.metis.storage.grpc.lib.types.IdentityPB;

import java.util.List;


/**
 * 该类负责将数据库数据转换成proto接口所需数据结构
 */
public interface ConvertorService {

    IdentityPB toProtoIdentityPB(OrgInfo orgInfo);

    Base.Organization toProtoOrganization(OrgInfo orgInfo);

    List<com.platon.metis.storage.grpc.lib.types.TaskEvent> toProtoTaskEvent(List<com.platon.metis.storage.dao.entity.TaskEvent> taskEventList);

    com.platon.metis.storage.grpc.lib.types.TaskEvent toProtoTaskEvent(com.platon.metis.storage.dao.entity.TaskEvent taskEvent);

    com.platon.metis.storage.grpc.lib.api.MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile);

    List<MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList);

    com.platon.metis.storage.grpc.lib.types.MetadataPB toProtoMetadataPB(MetaData dataFile);

    List<com.platon.metis.storage.grpc.lib.types.MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList);

    com.platon.metis.storage.grpc.lib.types.TaskPB toTaskPB(TaskInfo taskInfo);

    List<com.platon.metis.storage.grpc.lib.types.TaskPB> toTaskPB(List<TaskInfo> taskInfoList);
}
