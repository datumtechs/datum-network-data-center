package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.api.*;
import com.platon.rosettanet.storage.grpc.lib.common.*;
import com.platon.rosettanet.storage.grpc.lib.types.*;
import io.grpc.StatusRuntimeException;
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
public class TaskServiceGrpcStubTest {
    @GrpcClient("inProcess")
    private TaskServiceGrpc.TaskServiceBlockingStub taskServiceBlockingStub;

    @Test
    public void saveTask() {
        log.info("start to test saveTask()...");

        TaskDetail request = TaskDetail.newBuilder()
                .setTaskId("taskId")
                .setUser("userId")
                .setUserType(UserType.User_LAT)
                .setTaskName("taskName")
                .setCreateAt(1623852296000L)
                .setEndAt(1623852296000L)
                .setState(TaskState.TaskState_Succeed)
                .setOperationCost(TaskResourceCostDeclare.newBuilder().setProcessor(1).setMemory(100L).setBandwidth(100L).build())
                .setSender(TaskOrganization.newBuilder().setIdentityId("org_id_5").setPartyId("partyId").build())
                .setAlgoSupplier(TaskOrganization.newBuilder().setIdentityId("algoSupplier").setPartyId("algoSupplierPartyId").build())

                .addDataSuppliers(TaskDataSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("dataSupper_id_1").setPartyId("dataSupper_partyId_1").build()).setMetadataId("metaDataId_1")
                        .addColumns(MetadataColumn.newBuilder().setCIndex(1).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(2).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(3).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(4).build()))
                .addDataSuppliers(TaskDataSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("dataSupper_id_2").setPartyId("dataSupper_partyId_2").build()).setMetadataId("metaDataId_2")
                        .addColumns(MetadataColumn.newBuilder().setCIndex(1).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(2).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(3).build()))
                .addDataSuppliers(TaskDataSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("dataSupper_id_3").setPartyId("dataSupper_partyId_3").build()).setMetadataId("metaDataId_3")
                        .addColumns(MetadataColumn.newBuilder().setCIndex(1).build())
                        .addColumns(MetadataColumn.newBuilder().setCIndex(2).build()))

                .addPowerSuppliers(TaskPowerSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("power_1").setPartyId("power_party_1").build()).setResourceUsedOverview(ResourceUsageOverview.newBuilder().setUsedMem(100L).setUsedProcessor(1).setUsedBandwidth(1000L).build()).build())
                .addPowerSuppliers(TaskPowerSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("power_2").setPartyId("power_party_2").build()).setResourceUsedOverview(ResourceUsageOverview.newBuilder().setUsedMem(200L).setUsedProcessor(2).setUsedBandwidth(2000L).build()).build())
                .addPowerSuppliers(TaskPowerSupplier.newBuilder().setOrganization(TaskOrganization.newBuilder().setIdentityId("power_3").setPartyId("power_party_3").build()).setResourceUsedOverview(ResourceUsageOverview.newBuilder().setUsedMem(300L).setUsedProcessor(3).setUsedBandwidth(3000L).build()).build())

                .addReceivers(TaskOrganization.newBuilder().setIdentityId("receiverId_1").setPartyId("receiverId_1_party").build())
                .addReceivers(TaskOrganization.newBuilder().setIdentityId("receiverId_2").setPartyId("receiverId_2_party").build())
                .addReceivers(TaskOrganization.newBuilder().setIdentityId("receiverId_3").setPartyId("receiverId_3_party").build())
                .addReceivers(TaskOrganization.newBuilder().setIdentityId("receiverId_24").setPartyId("receiverId_4_party").build())
                .build();

        SimpleResponse response;
        try {
            response = taskServiceBlockingStub.saveTask(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: " + e.getMessage(), e);
            throw e;
        }
        log.info("saveTask(), response.status:{}", response.getStatus());
    }

    @Test
    public void getDetailTask() {
        log.info("start to test getDetailTask()...");

        DetailTaskRequest request = DetailTaskRequest.newBuilder().setTaskId("taskId_000006").build();
        TaskDetail taskDetail = taskServiceBlockingStub.getDetailTask(request);

        log.info("getDetailTask(), response:{}", taskDetail);
    }

    @Test
    public void listTask() {
        log.info("start to test listTask()...");

        LocalDateTime lastUpdated = LocalDateTime.parse("2021-09-08 08:49:24",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        TaskListRequest request = TaskListRequest.newBuilder().setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000).build();
        TaskListResponse response = taskServiceBlockingStub.listTask(request);

        log.info("listTask(), response.size:{}", response.getTaskDetailsList().size());
    }

    @Test
    public void listTaskByIdentityId() {
        log.info("start to test listTaskByIdentityId()...");
        LocalDateTime lastUpdated = LocalDateTime.parse("2021-09-08 08:49:24",  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        TaskListByIdentityRequest request = TaskListByIdentityRequest.newBuilder()
                .setIdentityId("identityId_000019")
                .setLastUpdated(lastUpdated.toEpochSecond(ZoneOffset.UTC)*1000)
                .build();
        TaskListResponse response = taskServiceBlockingStub.listTaskByIdentity(request);

        log.info("listTaskByIdentityId(), response.size:{}", response.getTaskDetailsList().size());
    }

    @Test
    public void listTaskEvent() {
        log.info("start to test listTaskEvent()...");

        TaskEventRequest request = TaskEventRequest.newBuilder().setTaskId("taskId").build();
        TaskEventResponse response = taskServiceBlockingStub.listTaskEvent(request);

        log.info("listTaskEvent(), response.size:{}", response.getTaskEventsList().size());
    }

    @Test
    public void revokeIdentityJoin() {
        log.info("start to test revokeIdentityJoin()...");

        TaskEventRequest request = TaskEventRequest.newBuilder().setTaskId("identityId_000003").build();
        TaskEventResponse response = taskServiceBlockingStub.listTaskEvent(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }
}
