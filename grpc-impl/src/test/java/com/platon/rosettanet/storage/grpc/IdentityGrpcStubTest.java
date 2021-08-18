package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.*;
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
                .setMember(Organization.newBuilder().setIdentityId("org_id_5").setName("org_name_4").setNodeId("node_id_4").build())
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

        IdentityListRequest request = IdentityListRequest.newBuilder()
                .setLastUpdateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
        IdentityListResponse response = identityServiceBlockingStub.getIdentityList(request);

        log.info("getIdentityList(), response:{}", response.getIdentityListList());
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

        RevokeIdentityJoinRequest request = RevokeIdentityJoinRequest.newBuilder()
                .setMember(Organization.newBuilder().setIdentityId("org_id_4").setName("org_name_4").setNodeId("node_id_4").build())
                .build();
        SimpleResponse response = identityServiceBlockingStub.revokeIdentityJoin(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }
}
