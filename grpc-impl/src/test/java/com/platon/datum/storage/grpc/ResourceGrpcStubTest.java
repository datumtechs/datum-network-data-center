package com.platon.datum.storage.grpc;


import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.ResourceData;
import com.platon.datum.storage.grpc.datacenter.api.Resource;
import com.platon.datum.storage.grpc.datacenter.api.ResourceServiceGrpc;
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
        IdentityData.Organization organization = IdentityData.Organization.newBuilder().setIdentityId("org_id_5").build();
        Resource.PublishPowerRequest request = Resource.PublishPowerRequest.newBuilder()
                .setPower(ResourceData.ResourcePB.newBuilder()
                        .setOwner(organization)
                        .setTotalBandwidth(20000L)
                        .setTotalMem(10000L)
                        .setTotalProcessor(10)
                        .build()
                ).build();
        Common.SimpleResponse response = resourceServiceBlockingStub.publishPower(request);

        log.info("publishPower(), response.status:{}", response.getStatus());
    }

    @Test
    public void syncPower() {
        log.info("start to test syncPower()...");
        Resource.SyncPowerRequest request = Resource.SyncPowerRequest.newBuilder()
                .setPower(ResourceData.LocalResourcePB.newBuilder()
                        .setDataId("powerId_000001_000001")
                        .setUsedProcessor(1000)
                        .setUsedMem(1000)
                        .setUsedBandwidth(1000)
                        .build())
                .build();
        Common.SimpleResponse response = resourceServiceBlockingStub.syncPower(request);

        log.info("syncPower(), response.status:{}", response.getStatus());
    }

    @Test
    public void revokePower() {
        log.info("start to test revokePower()...");
        Resource.RevokePowerRequest request = Resource.RevokePowerRequest.newBuilder()
                .setPowerId("test_power_id")
                .build();
        Common.SimpleResponse response = resourceServiceBlockingStub.revokePower(request);

        log.info("revokePower(), response.status:{}", response.getStatus());
    }


    @Test
    public void listPower() {
        log.info("start to test listPower()...");

        log.info("start to test getMetadataList()...");
        LocalDateTime lastUpdated = LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        Resource.ListPowerRequest request = Resource.ListPowerRequest
                .newBuilder()
                .setLastUpdated(lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPageSize(Long.MAX_VALUE)
                .build();
        Resource.ListPowerResponse response = resourceServiceBlockingStub.listPower(request);

        log.info("listPower(), response:{}", response);
    }

    @Test
    public void getPowerSummaryByIdentityId() {
        log.info("start to test getPowerSummaryByIdentityId()...");
        Resource.GetPowerSummaryByIdentityRequest request = Resource.GetPowerSummaryByIdentityRequest.newBuilder()
                .setIdentityId("identity_04fc711301f3c784d66955d98d399afb")
                .build();
        Resource.PowerSummaryResponse response = resourceServiceBlockingStub.getPowerSummaryByIdentityId(request);

        log.info("getPowerSummaryByIdentityId(), response:{}", response);
    }

    @Test
    public void listPowerSummary() {
        log.info("start to test listPowerSummary()...");

        LocalDateTime lastUpdated = LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();

        Resource.ListPowerSummaryResponse response = resourceServiceBlockingStub.listPowerSummary(request);

        log.info("listPowerSummary(), response:{}", response);
    }
}
