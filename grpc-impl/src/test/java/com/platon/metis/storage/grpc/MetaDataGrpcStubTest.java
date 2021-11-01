package com.platon.metis.storage.grpc;

import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.common.MetadataState;
import com.platon.metis.storage.grpc.lib.common.OriginFileType;
import com.platon.metis.storage.grpc.lib.common.SimpleResponse;
import com.platon.metis.storage.grpc.lib.types.MetadataColumn;
import com.platon.metis.storage.grpc.lib.types.MetadataPB;
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
    public void saveMetaData() {
        log.info("start to test saveMetaData()...");

        SaveMetadataRequest request = SaveMetadataRequest.newBuilder()
                .setMetadata(MetadataPB.newBuilder()
                        .setIdentityId("org_id_5")
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
                        .addMetadataColumns(MetadataColumn.newBuilder().setCIndex(0).setCName("col_name_0").setCType("long").setCSize(100).setCComment("comment_0").build())
                        .addMetadataColumns(MetadataColumn.newBuilder().setCIndex(1).setCName("col_name_1").setCType("string").setCSize(100).setCComment("comment_1").build())
                        .addMetadataColumns(MetadataColumn.newBuilder().setCIndex(2).setCName("col_name_2").setCType("date").setCSize(100).setCComment("comment_2").build())
                        .addMetadataColumns(MetadataColumn.newBuilder().setCIndex(3).setCName("col_name_3").setCType("bool").setCSize(100).setCComment("comment_3").build())
                        .build()
                ).build();
        SimpleResponse response = metaDataServiceBlockingStub.saveMetadata(request);

        log.info("saveMetaData(), response.status:{}", response.getStatus());
    }

    @Test
    public void listMetaDataSummary() {
        log.info("start to test listMetaDataSummary()...");
        com.google.protobuf.Empty request = com.google.protobuf.Empty.getDefaultInstance();
        ListMetadataSummaryResponse response = metaDataServiceBlockingStub.listMetadataSummary(request);

        log.info("listMetaDataSummary(), response:{}", response.getMetadataSummariesList());
    }

    @Test
    public void listMetadata() {
        log.info("start to test listMetadata()...");
        LocalDateTime lastUpdated = LocalDateTime.parse("2021-08-28 08:45:37",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        ListMetadataRequest request = ListMetadataRequest.newBuilder().setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000).build();
        ListMetadataResponse response = metaDataServiceBlockingStub.listMetadata(request);

        log.info("listMetadata(), response:{}", response.getMetadataList());
    }

    @Test
    public void findMetadataById() {
        log.info("start to test findMetadataById()...");
        FindMetadataByIdRequest request = FindMetadataByIdRequest.newBuilder().setMetadataId("test_meta_data_2").build();
        FindMetadataByIdResponse response = metaDataServiceBlockingStub.findMetadataById(request);

        log.info("findMetadataById(), response:{}", response.getMetadata());
    }

    @Test
    public void revokeMetaData() {
        log.info("start to test revokeMetaData()...");
        RevokeMetadataRequest request = RevokeMetadataRequest.newBuilder().setMetadataId("test_meta_data_2").build();
        SimpleResponse response = metaDataServiceBlockingStub.revokeMetadata(request);

        log.info("revokeMetaData(), response.status:{}", response.getStatus());
    }
}