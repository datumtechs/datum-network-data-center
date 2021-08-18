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
public class ResourceGrpcStubTest {
    @GrpcClient("inProcess")
    private ResourceServiceGrpc.ResourceServiceBlockingStub resourceServiceBlockingStub;

    @Test
    public void publishPower() {
        log.info("start to test publishPower()...");
        PublishPowerRequest request = PublishPowerRequest.newBuilder()
                .setOwner(Organization.newBuilder().setIdentityId("org_id_4").build())
                .setPowerId("test_power_id")
                .setInformation(PurePower.newBuilder().setProcessor(10).setMem(10000L).setBandwidth(20000L).build())
                .build();
        SimpleResponse response = resourceServiceBlockingStub.publishPower(request);

        log.info("publishPower(), response.status:{}", response.getStatus());
    }

    @Test
    public void syncPower() {
        log.info("start to test syncPower()...");
        SyncPowerRequest request = SyncPowerRequest.newBuilder()
                .setPower(Power.newBuilder()
                        .setPowerId("test_power_id")
                        .setInformation(ResourceUsed.newBuilder()
                                .setUsedProcessor(10)
                                .setUsedMem(1000)
                                .setUsedBandwidth(100)
                                .build())
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
    public void getPowerList() {
        log.info("start to test getPowerList()...");
        PowerListRequest request = PowerListRequest.newBuilder()
                .setLastUpdateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
        PowerListResponse response = resourceServiceBlockingStub.getPowerList(request);

        log.info("getPowerList(), response:{}", response);
    }

    @Test
    public void getPowerSummaryByIdentityId() {
        log.info("start to test getPowerSummaryByIdentityId()...");
        PowerSummaryByIdentityRequest request = PowerSummaryByIdentityRequest.newBuilder()
                .setIdentityId("identity_d7b7acd0d7a444f6b1496722eeff792b")
                .build();
        PowerTotalSummaryResponse response = resourceServiceBlockingStub.getPowerSummaryByIdentityId(request);

        log.info("getPowerSummaryByIdentityId(), response:{}", response);
    }

    @Test
    public void getPowerTotalSummaryList() {
        log.info("start to test getPowerSummaryByNodeId()...");
        com.google.protobuf.Empty request = com.google.protobuf.Empty.getDefaultInstance();

        PowerTotalSummaryListResponse response = resourceServiceBlockingStub.getPowerTotalSummaryList(request);

        log.info("getPowerTotalSummaryList(), response:{}", response);
    }
}
