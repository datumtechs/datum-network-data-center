package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MetaDataGrpcStubTest {
    @GrpcClient("inProcess")
    private MetaDataServiceGrpc.MetaDataServiceBlockingStub metaDataServiceBlockingStub;


    @Test
    public void metaDataSave() {
        log.info("start to test metaDataSave()...");

        MetaDataSaveRequest request = MetaDataSaveRequest.newBuilder()
                .setOwner(Organization.newBuilder().setIdentityId("org_id_5").setName("org_name_4").setNodeId("node_id_4").build())
                .setMetaSummary(MetaDataSummary.newBuilder()
                        .setHasTitle(true)
                        .setOriginId("data_file_origin_id_2")
                        .setSize(1000000000L)
                        .setFilePath("/opt/usr/data")
                        .setFileType("cvs")
                        .setTableName("table_name_2.cvs")
                        .setMetaDataId("test_meta_data_2")
                        .setColumns(100)
                        .setRows(10000)
                        .setDesc("desc")
                        .setState("released")
                        .build())
                .addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(0).setCname("col_name_0").setCtype("long").setCsize(100).setCcomment("comment_0").build())
                .addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(1).setCname("col_name_1").setCtype("string").setCsize(100).setCcomment("comment_1").build())
                .addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(2).setCname("col_name_2").setCtype("date").setCsize(100).setCcomment("comment_2").build())
                .addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(3).setCname("col_name_3").setCtype("bool").setCsize(100).setCcomment("comment_3").build())
                .build();
        SimpleResponse response = metaDataServiceBlockingStub.metaDataSave(request);

        log.info("metaDataSave(), response.status:{}", response.getStatus());
    }

    @Test
    public void getMetaDataSummaryList() {
        log.info("start to test getMetaDataSummaryList()...");
        com.google.protobuf.Empty request = com.google.protobuf.Empty.getDefaultInstance();
        MetaDataSummaryListResponse response = metaDataServiceBlockingStub.getMetaDataSummaryList(request);

        log.info("getMetaDataSummaryList(), response:{}", response.getMetadataSummaryListList());
    }

    @Test
    public void getMetadataList() {
        log.info("start to test getMetadataList()...");
        MetadataListRequest request = MetadataListRequest.newBuilder().setLastUpdateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()).build();
        MetadataListResponse response = metaDataServiceBlockingStub.getMetadataList(request);

        log.info("getMetadataList(), response:{}", response.getMetadataListList());
    }

    @Test
    public void getMetadataById() {
        log.info("start to test getMetadataById()...");
        MetadataByIdRequest request = MetadataByIdRequest.newBuilder().setMetadataId("test_meta_data_1").build();
        MetadataByIdResponse response = metaDataServiceBlockingStub.getMetadataById(request);

        log.info("getMetadataById(), response:{}", response.getMetadata());
    }

    @Test
    public void revokeMetaData() {
        log.info("start to test revokeMetaData()...");
        RevokeMetaDataRequest request = RevokeMetaDataRequest.newBuilder().setMetaDataId("test_meta_data_1").build();
        SimpleResponse response = metaDataServiceBlockingStub.revokeMetaData(request);

        log.info("revokeMetaData(), response.status:{}", response.getStatus());
    }
}
