package com.platon.datum.storage.grpc;

import com.google.protobuf.ByteString;
import com.platon.datum.storage.grpc.carrier.types.Common;
import com.platon.datum.storage.grpc.carrier.types.IdentityData;
import com.platon.datum.storage.grpc.carrier.types.Metadata;
import com.platon.datum.storage.grpc.common.constant.CarrierEnum;
import com.platon.datum.storage.grpc.datacenter.api.Identity;
import com.platon.datum.storage.grpc.datacenter.api.IdentityServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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

        Identity.SaveIdentityRequest request = Identity.SaveIdentityRequest.newBuilder()
                .setInformation(IdentityData.IdentityPB.newBuilder().setIdentityId("org_id_5").setNodeName("org_name_4").setNodeId("node_id_4").build())
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

        Identity.ListIdentityRequest request = Identity.ListIdentityRequest
                .newBuilder()
                .setLastUpdated(lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPageSize(Long.MAX_VALUE)
                .build();
        Identity.ListIdentityResponse response = identityServiceBlockingStub.listIdentity(request);

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

        Identity.RevokeIdentityRequest request = Identity.RevokeIdentityRequest.newBuilder()
                .setIdentityId("identity_04fc711301f3c784d66955d98d399afb")
                .build();
        Common.SimpleResponse response = identityServiceBlockingStub.revokeIdentity(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }


    @Test
    public void SaveMetadataAuthority() throws DecoderException {
        log.info("start to test SaveMetadataAuthority()...");

        String signStr = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        byte[] bytes = Hex.decodeHex(signStr);
        ByteString sign = ByteString.copyFromUtf8(signStr);
        sign = ByteString.EMPTY;

        Identity.MetadataAuthorityRequest request = Identity.MetadataAuthorityRequest.newBuilder()
                .setMetadataAuthority(Metadata.MetadataAuthorityPB.newBuilder()

                        .setMetadataAuthId("metaDataAuthId_01")
                        .setUser("userId_01")
                        .setDataId("metadataId_01")
                        .setDataStatus(CarrierEnum.DataStatus.forNumber(1))
                        .setUserType(CarrierEnum.UserType.forNumber(1))
                        .setAuth(Metadata.MetadataAuthority.newBuilder()
                                .setMetadataId("metadataId_01")
                                .setOwner(IdentityData.Organization.newBuilder().setIdentityId("identityId_01").build())
                                .setUsageRule(Metadata.MetadataUsageRule.newBuilder()
                                        .setUsageType(CarrierEnum.MetadataUsageType.forNumber(2))
                                        .setTimes(10)
                                        .setStartAt(0)
                                        .setEndAt(0)
                                        .build())
                        )
                        .setAuditOption(CarrierEnum.AuditMetadataOption.forNumber(0))
                        .setAuditSuggestion(StringUtils.trimToEmpty("pending suggestion"))
                        .setUsedQuo(Metadata.MetadataUsedQuo.newBuilder().setUsageType(CarrierEnum.MetadataUsageType.forNumber(2))
                                .setExpire(false)
                                .setUsedTimes(4)
                                .build())

                        .setApplyAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .setAuditAt(0)
                        .setState(CarrierEnum.MetadataAuthorityState.forNumber(2))
                        .setSign(sign)
                ).build();

        Common.SimpleResponse response = identityServiceBlockingStub.saveMetadataAuthority(request);

        log.info("SaveMetadataAuthority(), response.status:{}", response.getStatus());
    }

    @Test
    public void findMetadataAuthority() {
        log.info("start to test findMetadataAuthority()...");

        Identity.FindMetadataAuthorityRequest request = Identity.FindMetadataAuthorityRequest.newBuilder()
                .setMetadataAuthId("metaDataAuthId_01")
                .build();

        Identity.FindMetadataAuthorityResponse response = identityServiceBlockingStub.findMetadataAuthority(request);

        log.info("findMetadataAuthority(), response.getMetadataAuthority().getAuth().getOwner().getIdentityId():{}", response.getMetadataAuthority().getAuth().getOwner().getIdentityId());
    }

    @Test
    public void updateMetadataAuthority() throws DecoderException {
        log.info("start to test updateMetadataAuthority()...");
        String sign = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        byte[] bytes = Hex.decodeHex(sign);
        Identity.MetadataAuthorityRequest request = Identity.MetadataAuthorityRequest.newBuilder()
                .setMetadataAuthority(Metadata.MetadataAuthorityPB.newBuilder()
                        .setMetadataAuthId("metaDataAuthId_01")
                        .setUser("userId_01")
                        .setDataId("metadataId_01")
                        .setDataStatus(CarrierEnum.DataStatus.forNumber(1))
                        .setUserType(CarrierEnum.UserType.forNumber(1))
                        .setAuth(Metadata.MetadataAuthority.newBuilder()
                                .setMetadataId("metadataId_01")
                                .setOwner(IdentityData.Organization.newBuilder().setIdentityId("identityId_01").build())
                                .setUsageRule(Metadata.MetadataUsageRule.newBuilder()
                                        .setUsageType(CarrierEnum.MetadataUsageType.forNumber(2))
                                        .setTimes(10)
                                        .setStartAt(0)
                                        .setEndAt(0)
                                        .build())
                        )
                        .setAuditOption(CarrierEnum.AuditMetadataOption.forNumber(1))
                        .setAuditSuggestion(StringUtils.trimToEmpty("approved suggestion"))
                        .setUsedQuo(Metadata.MetadataUsedQuo.newBuilder().setUsageType(CarrierEnum.MetadataUsageType.forNumber(2))
                                .setExpire(false)
                                .setUsedTimes(4)
                                .build())

                        .setApplyAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .setAuditAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .setState(CarrierEnum.MetadataAuthorityState.forNumber(2))
                        .setSign(ByteString.copyFrom(bytes))
                ).build();

        Common.SimpleResponse response = identityServiceBlockingStub.updateMetadataAuthority(request);

        log.info("updateMetadataAuthority(), response.status:{}", response.getStatus());
    }

    @Test
    public void listMetadataAuthority() {
        LocalDateTime lastUpdated = LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Identity.ListMetadataAuthorityRequest request = Identity.ListMetadataAuthorityRequest
                .newBuilder()
                .setIdentityId("identity_a3876b82060f4eafbca7257692f1b285")
                .setLastUpdated(lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPageSize(Long.MAX_VALUE)
                .build();

        Identity.ListMetadataAuthorityResponse response = identityServiceBlockingStub.listMetadataAuthority(request);

        log.info("updateMetadataAuthority(), response.getMetadataAuthoritiesList.size:{}", response.getMetadataAuthoritiesList().size());
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