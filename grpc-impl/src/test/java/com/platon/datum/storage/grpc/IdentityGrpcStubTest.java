package com.platon.datum.storage.grpc;

import carrier.types.Common;
import carrier.types.Identitydata;
import com.google.protobuf.ByteString;
import datacenter.api.Auth;
import datacenter.api.IdentityServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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

        Auth.SaveIdentityRequest request = Auth.SaveIdentityRequest.newBuilder()
                .setInformation(Identitydata.IdentityPB.newBuilder().setIdentityId("org_id_5").setNodeName("org_name_4").setNodeId("node_id_4").build())
                .build();

        try {
            Common.SimpleResponse response = identityServiceBlockingStub.saveIdentity(request);
            log.info("saveIdentity(), response.status:{}", response.getStatus());

        } catch (StatusRuntimeException e) {
            com.google.rpc.Status status = io.grpc.protobuf.StatusProto.fromThrowable(e);
            System.out.println("status.code:" + status.getCode());
            System.out.println("status.message:" + status.getMessage());
        }
    }


    @Test
    public void getIdentityList() {
        log.info("start to test getIdentityList()...");

        LocalDateTime lastUpdated = LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Auth.ListIdentityRequest request = Auth.ListIdentityRequest
                .newBuilder()
                .setLastUpdated(lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPageSize(Long.MAX_VALUE)
                .build();
        Auth.ListIdentityResponse response = identityServiceBlockingStub.listIdentity(request);

        log.info("getIdentityList().size: {}", response.getIdentitiesList().size());
    }


    @Test
    public void getIdentityList2() {
        List<String> test = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "m", "n");
        List<String> expected = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "M", "N");
        for (int i = 0; i < 100; i++) {
            List<String> upperList = test.parallelStream().map(s -> {
                try {
                    Thread.sleep(RandomUtils.nextInt(0, 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return s.toUpperCase();
            }).collect(Collectors.toList());
            //assertTrue(Iterables.elementsEqual(expected, upperList));
            assertEquals(expected, upperList);
        }
    }

    @Test
    public void revokeIdentityJoin() {
        log.info("start to test revokeIdentityJoin()...");

        Auth.RevokeIdentityRequest request = Auth.RevokeIdentityRequest.newBuilder()
                .setIdentityId("identity_04fc711301f3c784d66955d98d399afb")
                .build();
        Common.SimpleResponse response = identityServiceBlockingStub.revokeIdentity(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }

    @Test
    public void signTest() throws DecoderException {
        String sign = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        byte[] bytes = Hex.decodeHex(sign);
        System.out.println("bytes.length=" + ByteString.copyFrom(bytes).toByteArray().length);

        sign = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        bytes = Hex.decodeHex(sign);
        System.out.println("bytes.length=" + ByteString.copyFrom(bytes).toByteArray().length);

        String signStr = "0xb3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";

        ByteString byteString = ByteString.copyFrom(bytes);
        System.out.println("bytes.length=" + byteString.toByteArray().length);
    }
}
