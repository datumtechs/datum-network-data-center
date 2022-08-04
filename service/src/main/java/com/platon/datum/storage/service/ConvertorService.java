package com.platon.datum.storage.service;

import carrier.types.Identitydata;
import carrier.types.Metadata;
import carrier.types.Taskdata;
import com.platon.datum.storage.dao.entity.*;

import java.util.List;


/**
 * 该类负责将数据库数据转换成proto接口所需数据结构
 */
public interface ConvertorService {

    Identitydata.IdentityPB toProtoIdentityPB(OrgInfo orgInfo);

    Identitydata.Organization toProtoOrganization(OrgInfo orgInfo);

    List<Taskdata.TaskEvent> toProtoTaskEvent(List<TaskEvent> taskEventList);

    Taskdata.TaskEvent toProtoTaskEvent(TaskEvent taskEvent);

    datacenter.api.Metadata.MetadataSummaryOwner toProtoMetaDataSummaryWithOwner(MetaData dataFile);

    List<datacenter.api.Metadata.MetadataSummaryOwner> toProtoMetaDataSummaryWithOwner(List<MetaData> dataFileList);

    Metadata.MetadataPB toProtoMetadataPB(MetaData dataFile);

    List<Metadata.MetadataPB> toProtoMetadataPB(List<MetaData> dataFileList);

    Taskdata.TaskPB toTaskPB(TaskInfo taskInfo);

    List<Taskdata.TaskPB> toTaskPB(List<TaskInfo> taskInfoList);

    Metadata.MetadataAuthorityPB toProtoMetadataAuthorityPB(MetaDataAuth metaDataAuth);
}
