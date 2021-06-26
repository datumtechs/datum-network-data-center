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
public class IdentityGrpcStubTest {
    @GrpcClient("inProcess")
    private IdentityServiceGrpc.IdentityServiceBlockingStub identityServiceBlockingStub;


    @Test
    public void saveIdentity() {
        log.info("start to test saveIdentity()...");

        SaveIdentityRequest request = SaveIdentityRequest.newBuilder()
                .setMember(Organization.newBuilder().setIdentityId("org_id_4").setName("org_name_4").setNodeId("node_id_4").build())
                .setCredential("DID")
                .build();
        SimpleResponse response = identityServiceBlockingStub.saveIdentity(request);

        log.info("saveIdentity(), response.status:{}", response.getStatus());
    }

    @Test
    public void getIdentityList() {
        log.info("start to test getIdentityList()...");

        IdentityListRequest request = IdentityListRequest.newBuilder()
                .setLastUpdateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
        IdentityListResponse response = identityServiceBlockingStub.getIdentityList(request);

        log.info("getIdentityList(), response:{}", response.getIdentityListList());
    }

    @Test
    public void revokeIdentityJoin() {
        log.info("start to test revokeIdentityJoin()...");

        RevokeIdentityJoinRequest request = RevokeIdentityJoinRequest.newBuilder()
                .setMember(Organization.newBuilder().setIdentityId("org_id_4").setName("org_name_4").setNodeId("node_id_4").build())
                .build();
        SimpleResponse response = identityServiceBlockingStub.revokeIdentityJoin(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }
}
