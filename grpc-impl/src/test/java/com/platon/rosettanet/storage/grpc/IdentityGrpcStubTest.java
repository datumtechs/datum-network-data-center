package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.Organization;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


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
                .setMember(Organization.newBuilder().setIdentityId("org_id_5").setNodeName("org_name_4").setNodeId("node_id_4").build())
                .setCredential("DID")
                .build();

        try {
            SimpleResponse response = identityServiceBlockingStub.saveIdentity(request);
            log.info("saveIdentity(), response.status:{}", response.getStatus());

        }catch(StatusRuntimeException e){
            com.google.rpc.Status status = io.grpc.protobuf.StatusProto.fromThrowable(e);
            System.out.println("status.code:" + status.getCode());
            System.out.println("status.message:" + status.getMessage());
        }
    }



    @Test
    public void getIdentityList() {
        log.info("start to test getIdentityList()...");

        LocalDateTime lastUpdated = LocalDateTime.parse("2021-09-08 08:49:24",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ListIdentityRequest request = ListIdentityRequest.newBuilder()
                .setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000)
                .build();
        ListIdentityResponse response = identityServiceBlockingStub.listIdentity(request);

        log.info("getIdentityList().size: {}", response.getIdentitiesList().size());
    }


    @Test
    public void getIdentityList2() {
        List<String> test = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","m","n");
        List<String> expected = Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","M","N");
        for(int i=0; i<100; i++){
            List<String> upperList = test.parallelStream().map(s->{
                try {
                    Thread.sleep(RandomUtils.nextInt(0,100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return s.toUpperCase();}).collect(Collectors.toList());
            //assertTrue(Iterables.elementsEqual(expected, upperList));
            assertEquals(expected, upperList);
         }
    }

    @Test
    public void revokeIdentityJoin() {
        log.info("start to test revokeIdentityJoin()...");

        RevokeIdentityRequest request = RevokeIdentityRequest.newBuilder()
                .setIdentityId("org_id_5")
                .build();
        SimpleResponse response = identityServiceBlockingStub.revokeIdentity(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }
}
