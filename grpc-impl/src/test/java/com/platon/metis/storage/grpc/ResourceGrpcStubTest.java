package com.platon.metis.storage.grpc;


import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.common.SimpleResponse;
import com.platon.metis.storage.grpc.lib.types.LocalResourcePB;
import com.platon.metis.storage.grpc.lib.types.ResourcePB;
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
public class ResourceGrpcStubTest {
    @GrpcClient("inProcess")
    private ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub;

    @Test
    public void publishPower() {
        log.info("start to test publishPower()...");
        PublishPowerRequest request = PublishPowerRequest.newBuilder()
                .setPower(ResourcePB.newBuilder()
                        .setIdentityId("org_id_5")
                        .setTotalBandwidth(20000L)
                        .setTotalMem(10000L)
                        .setTotalProcessor(10)
                        .build()
                ).build();
        SimpleResponse response = resourceServiceBlockingStub.publishPower(request);

        log.info("publishPower(), response.status:{}", response.getStatus());
    }

    @Test
    public void syncPower() {
        log.info("start to test syncPower()...");
        SyncPowerRequest request = SyncPowerRequest.newBuilder()
                .setPower(LocalResourcePB.newBuilder()
                        .setDataId("powerId_000001_000001")
                        .setUsedProcessor(1000)
                        .setUsedMem(1000)
                        .setUsedBandwidth(1000)
                        .build())
                .build();
        SimpleResponse response = resourceServiceBlockingStub.syncPower(request);

        log.info("syncPower(), response.status:{}", response.getStatus());
    }

    @Test
    public void revokePower() {
        log.info("start to test revokePower()...");
        RevokePowerRequest request = RevokePowerRequest.newBuilder()
                .setPowerId("test_power_id")
                .build();
        SimpleResponse response = resourceServiceBlockingStub.revokePower(request);

        log.info("revokePower(), response.status:{}", response.getStatus());
    }


    @Test
    public void listPower() {
        log.info("start to test listPower()...");

        log.info("start to test getMetadataList()...");
        LocalDateTime lastUpdated = LocalDateTime.parse("2021-09-16 09:00:57",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        ListPowerRequest request = ListPowerRequest.newBuilder()
                .setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000)
                .build();
        ListPowerResponse response = resourceServiceBlockingStub.listPower(request);

        log.info("listPower(), response:{}", response);
    }

    @Test
    public void getPowerSummaryByIdentityId() {
        log.info("start to test getPowerSummaryByIdentityId()...");
        GetPowerSummaryByIdentityRequest request = GetPowerSummaryByIdentityRequest.newBuilder()
                .setIdentityId("identity_04fc711301f3c784d66955d98d399afb")
                .build();
        PowerSummaryResponse response = resourceServiceBlockingStub.getPowerSummaryByIdentityId(request);

        log.info("getPowerSummaryByIdentityId(), response:{}", response);
    }

    @Test
    public void listPowerSummary() {
        log.info("start to test listPowerSummary()...");
        com.google.protobuf.Empty request = com.google.protobuf.Empty.getDefaultInstance();

        ListPowerSummaryResponse response = resourceServiceBlockingStub.listPowerSummary(request);

        log.info("listPowerSummary(), response:{}", response);
    }
}
