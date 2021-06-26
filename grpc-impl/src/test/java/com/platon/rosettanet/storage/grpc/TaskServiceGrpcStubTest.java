package com.platon.rosettanet.storage.grpc;

import com.platon.rosettanet.storage.grpc.lib.*;
import io.grpc.StatusRuntimeException;
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
public class TaskServiceGrpcStubTest {
    @GrpcClient("inProcess")
    private TaskServiceGrpc.TaskServiceBlockingStub taskServiceBlockingStub;

    @Test
    public void saveTask() {
        log.info("start to test saveTask()...");

        TaskDetail request = TaskDetail.newBuilder()
                .setTaskId("taskId")
                .setTaskName("taskName")
                .setCreateAt(1623852296000L)
                .setEndAt(1623852296000L)
                .setState("pending")
                .setOperationCost(TaskOperationCostDeclare.newBuilder().setCostProcessor(1).setCostMem(100L).setCostBandwidth(100L).build())
                .setOwner(Organization.newBuilder().setIdentityId("ownerId").build())
                .setAlgoSupplier(Organization.newBuilder().setIdentityId("algoSupplier").build())
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_1").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(0).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_1").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(1).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_1").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(2).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_1").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(3).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_2").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(0).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_2").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(1).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_2").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(2).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_3").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(0).build()))
                .addDataSupplier(TaskDataSupplier.newBuilder().setMetaId("metaDataId_3").addColumnMeta(MetaDataColumnDetail.newBuilder().setCindex(1).build()))

                .addPowerSupplier(TaskPowerSupplier.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("power_1").build()).setPowerInfo(ResourceUsedDetail.newBuilder().setUsedMem(100L).setUsedProcessor(1).setUsedBandwidth(1000L).build()).build())
                .addPowerSupplier(TaskPowerSupplier.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("power_2").build()).setPowerInfo(ResourceUsedDetail.newBuilder().setUsedMem(200L).setUsedProcessor(2).setUsedBandwidth(2000L).build()).build())
                .addPowerSupplier(TaskPowerSupplier.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("power_3").build()).setPowerInfo(ResourceUsedDetail.newBuilder().setUsedMem(300L).setUsedProcessor(3).setUsedBandwidth(3000L).build()).build())

                .addReceivers(TaskResultReceiver.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("receiverId_1").build()).addProvider(Organization.newBuilder().setIdentityId("receiverId_1_provider_1").build()).build())
                .addReceivers(TaskResultReceiver.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("receiverId_1").build()).addProvider(Organization.newBuilder().setIdentityId("receiverId_1_provider_2").build()).build())
                .addReceivers(TaskResultReceiver.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("receiverId_2").build()).addProvider(Organization.newBuilder().setIdentityId("receiverId_2_provider_1").build()).build())
                .addReceivers(TaskResultReceiver.newBuilder().setMemberInfo(Organization.newBuilder().setIdentityId("receiverId_2").build()).addProvider(Organization.newBuilder().setIdentityId("receiverId_2_provider_2").build()).build())
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

        DetailTaskRequest request = DetailTaskRequest.newBuilder().setTaskId("taskId").build();
        TaskDetail taskDetail = taskServiceBlockingStub.getDetailTask(request);

        log.info("getDetailTask(), response:{}", taskDetail);
    }

    @Test
    public void listTask() {
        log.info("start to test listTask()...");

        TaskListRequest request = TaskListRequest.newBuilder().setLastUpdateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()).build();
        TaskListResponse response = taskServiceBlockingStub.listTask(request);

        log.info("listTask(), response:{}", response.getTaskListList());
    }

    @Test
    public void listTaskEvent() {
        log.info("start to test listTaskEvent()...");

        TaskEventRequest request = TaskEventRequest.newBuilder().setTaskId("taskId").build();
        TaskEventResponse response = taskServiceBlockingStub.listTaskEvent(request);

        log.info("listTaskEvent(), response:{}", response.getTaskEventListList());
    }

    @Test
    public void revokeIdentityJoin() {
        log.info("start to test revokeIdentityJoin()...");

        TaskEventRequest request = TaskEventRequest.newBuilder().setTaskId("taskId").build();
        TaskEventResponse response = taskServiceBlockingStub.listTaskEvent(request);

        log.info("revokeIdentityJoin(), response.status:{}", response.getStatus());
    }
}
