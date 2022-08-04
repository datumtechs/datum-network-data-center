package com.platon.datum.storage.grpc;

import carrier.types.Common;
import carrier.types.Identitydata;
import carrier.types.Metadata;
import com.google.protobuf.ByteString;
import common.constant.CarrierEnum;
import datacenter.api.Auth;
import datacenter.api.MetadataAuthServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
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
public class MetadataAuthGrpcStubTest {
    @GrpcClient("inProcess")
    private MetadataAuthServiceGrpc.MetadataAuthServiceBlockingStub metadataAuthServiceBlockingStub;


    @Test
    public void SaveMetadataAuthority() throws DecoderException {
        log.info("start to test SaveMetadataAuthority()...");

        String signStr = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        byte[] bytes = Hex.decodeHex(signStr);
        ByteString sign = ByteString.copyFromUtf8(signStr);
        sign = ByteString.EMPTY;

        Auth.MetadataAuthorityRequest request = Auth.MetadataAuthorityRequest.newBuilder()
                .setMetadataAuthority(Metadata.MetadataAuthorityPB.newBuilder()

                        .setMetadataAuthId("metaDataAuthId_01")
                        .setUser("userId_01")
                        .setDataId("metadataId_01")
                        .setDataStatus(CarrierEnum.DataStatus.forNumber(1))
                        .setUserType(CarrierEnum.UserType.forNumber(1))
                        .setAuth(Metadata.MetadataAuthority.newBuilder()
                                .setMetadataId("metadataId_01")
                                .setOwner(Identitydata.Organization.newBuilder().setIdentityId("identityId_01").build())
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

        Common.SimpleResponse response = metadataAuthServiceBlockingStub.saveMetadataAuthority(request);

        log.info("SaveMetadataAuthority(), response.status:{}", response.getStatus());
    }

    @Test
    public void findMetadataAuthority() {
        log.info("start to test findMetadataAuthority()...");

        Auth.FindMetadataAuthorityRequest request = Auth.FindMetadataAuthorityRequest.newBuilder()
                .setMetadataAuthId("metaDataAuthId_01")
                .build();

        Auth.FindMetadataAuthorityResponse response = metadataAuthServiceBlockingStub.findMetadataAuthority(request);

        log.info("findMetadataAuthority(), response.getMetadataAuthority().getAuth().getOwner().getIdentityId():{}", response.getMetadataAuthority().getAuth().getOwner().getIdentityId());
    }

    @Test
    public void updateMetadataAuthority() throws DecoderException {
        log.info("start to test updateMetadataAuthority()...");
        String sign = "b3d49c3804d7e71487b24744f11a968baa9dce99a8706f8c87dcaf482b9437d66e0115719cc9668d3b6472f6a629766dc0bb9625ffb698dc0b48496e996833a61b";
        byte[] bytes = Hex.decodeHex(sign);
        Auth.MetadataAuthorityRequest request = Auth.MetadataAuthorityRequest.newBuilder()
                .setMetadataAuthority(Metadata.MetadataAuthorityPB.newBuilder()
                        .setMetadataAuthId("metaDataAuthId_01")
                        .setUser("userId_01")
                        .setDataId("metadataId_01")
                        .setDataStatus(CarrierEnum.DataStatus.forNumber(1))
                        .setUserType(CarrierEnum.UserType.forNumber(1))
                        .setAuth(Metadata.MetadataAuthority.newBuilder()
                                .setMetadataId("metadataId_01")
                                .setOwner(Identitydata.Organization.newBuilder().setIdentityId("identityId_01").build())
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

        Common.SimpleResponse response = metadataAuthServiceBlockingStub.updateMetadataAuthority(request);

        log.info("updateMetadataAuthority(), response.status:{}", response.getStatus());
    }

    @Test
    public void listMetadataAuthority() {
        LocalDateTime lastUpdated = LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Auth.ListMetadataAuthorityRequest request = Auth.ListMetadataAuthorityRequest
                .newBuilder()
                .setIdentityId("identity_a3876b82060f4eafbca7257692f1b285")
                .setLastUpdated(lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli())
                .setPageSize(Long.MAX_VALUE)
                .build();

        Auth.ListMetadataAuthorityResponse response = metadataAuthServiceBlockingStub.listMetadataAuthority(request);

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
