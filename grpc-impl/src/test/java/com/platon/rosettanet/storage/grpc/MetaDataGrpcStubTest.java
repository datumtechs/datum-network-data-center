package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.MetadataState;
import com.platon.rosettanet.storage.grpc.lib.common.Organization;
import com.platon.rosettanet.storage.grpc.lib.common.OriginFileType;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataSummary;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MetaDataGrpcStubTest {
    @GrpcClient("inProcess")
    private MetadataServiceGrpc.MetadataServiceBlockingStub metaDataServiceBlockingStub;


    @Test
    public void metaDataSave() {
        log.info("start to test metaDataSave()...");

        MetadataSaveRequest request = MetadataSaveRequest.newBuilder()
                .setOwner(Organization.newBuilder().setIdentityId("org_id_5").setNodeName("org_name_4").setNodeId("node_id_4").build())
                .setMetaSummary(MetadataSummary.newBuilder()
                        .setHasTitle(true)
                        .setOriginId("data_file_origin_id_2")
                        .setSize(1000000000L)
                        .setFilePath("/opt/usr/data")
                        .setFileType(OriginFileType.FileType_CSV)
                        .setTableName("table_name_2.cvs")
                        .setMetadataId("test_meta_data_2")
                        .setColumns(100)
                        .setRows(10000)
                        .setDesc("desc")
                        .setState(MetadataState.MetadataState_Released)
                        .build())
                .addColumnMeta(MetadataColumn.newBuilder().setCIndex(0).setCName("col_name_0").setCType("long").setCSize(100).setCComment("comment_0").build())
                .addColumnMeta(MetadataColumn.newBuilder().setCIndex(1).setCName("col_name_1").setCType("string").setCSize(100).setCComment("comment_1").build())
                .addColumnMeta(MetadataColumn.newBuilder().setCIndex(2).setCName("col_name_2").setCType("date").setCSize(100).setCComment("comment_2").build())
                .addColumnMeta(MetadataColumn.newBuilder().setCIndex(3).setCName("col_name_3").setCType("bool").setCSize(100).setCComment("comment_3").build())
                .build();
        SimpleResponse response = metaDataServiceBlockingStub.metadataSave(request);

        log.info("metaDataSave(), response.status:{}", response.getStatus());
    }

    @Test
    public void getMetaDataSummaryList() {
        log.info("start to test getMetaDataSummaryList()...");
        com.google.protobuf.Empty request = com.google.protobuf.Empty.getDefaultInstance();
        MetadataSummaryListResponse response = metaDataServiceBlockingStub.getMetadataSummaryList(request);

        log.info("getMetaDataSummaryList(), response:{}", response.getMetadataSummariesList());
    }

    @Test
    public void getMetadataList() {
        log.info("start to test getMetadataList()...");
        LocalDateTime lastUpdated = LocalDateTime.parse("2021-09-08 08:45:37",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        MetadataListRequest request = MetadataListRequest.newBuilder().setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000).build();
        MetadataListResponse response = metaDataServiceBlockingStub.getMetadataList(request);

        log.info("getMetadataList(), response:{}", response.getMetadatasList());
    }

    @Test
    public void getMetadataById() {
        log.info("start to test getMetadataById()...");
        MetadataByIdRequest request = MetadataByIdRequest.newBuilder().setMetadataId("test_meta_data_2").build();
        MetadataByIdResponse response = metaDataServiceBlockingStub.getMetadataById(request);

        log.info("getMetadataById(), response:{}", response.getMetadata());
    }

    @Test
    public void revokeMetaData() {
        log.info("start to test revokeMetaData()...");
        RevokeMetadataRequest request = RevokeMetadataRequest.newBuilder().setMetadataId("test_meta_data_2").build();
        SimpleResponse response = metaDataServiceBlockingStub.revokeMetadata(request);

        log.info("revokeMetaData(), response.status:{}", response.getStatus());
    }
}
