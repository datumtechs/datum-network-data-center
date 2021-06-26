package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.dao.entity.Task;
import com.platon.rosettanet.storage.dao.entity.TaskEvent;
import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.*;
import com.platon.rosettanet.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class TaskGrpc extends TaskServiceGrpc.TaskServiceImplBase{

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMetaDataService taskMetaDataService;

    @Autowired
    private TaskMetaDataColumnService taskMetaDataColumnService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    @Autowired
    private TaskResultConsumerService taskResultConsumerService;

    @Autowired
    private TaskEventService taskEventService;

    @Autowired
    private OrgInfoService orgInfoService;


    @Autowired
    private ConvertorService convertorService;
    /**
     * <pre>
     * 存储任务
     * </pre>
     */
    @Transactional
    /**
     * <pre>
     * 存储任务
     * </pre>
     */
    public void saveTask(com.platon.rosettanet.storage.grpc.lib.TaskDetail request,
                         io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.SimpleResponse> responseObserver) {

        // 业务代码

        String taskId = request.getTaskId();

        //==任务
        Task task = new Task();

        task.setId(taskId);
        //任务名称
        task.setTaskName(request.getTaskName());

        //任务所有人
        task.setOwnerIdentityId(request.getOwner().getIdentityId());

        //所需资源
        task.setRequiredBandwidth(request.getOperationCost().getCostBandwidth());
        task.setRequiredCore(request.getOperationCost().getCostProcessor());
        task.setRequiredMemory(request.getOperationCost().getCostMem());
        task.setRequiredDuration(request.getOperationCost().getDuration());

        task.setUsedBandwidth(request.getOperationCost().getCostBandwidth());
        task.setUsedCore(request.getOperationCost().getCostProcessor());
        task.setUsedMemory(request.getOperationCost().getCostMem());

        //创建时间
        task.setCreateAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getCreateAt()), ZoneId.systemDefault()));
        //结束时间
        task.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getEndAt()), ZoneId.systemDefault()));

        //任务状态
        task.setStatus(request.getState());

        taskService.insert(task);


        //==任务的数据提供者
        List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
        Map<String, Boolean> metaDataIdMap = new HashMap<>();
        for (TaskDataSupplier dataSupplier : request.getDataSupplierList()) {
            metaDataIdMap.put(dataSupplier.getMetaId(), true);
            for(MetaDataColumnDetail columnDetail : dataSupplier.getColumnMetaList()){
                TaskMetaDataColumn dataProvider = new TaskMetaDataColumn();
                dataProvider.setTaskId(taskId);
                dataProvider.setMetaDataId(dataSupplier.getMetaId());
                dataProvider.setColumnIdx(columnDetail.getCindex());
                taskMetaDataColumnList.add(dataProvider);
            }
        }
        List<TaskMetaData> taskMetaDataList = metaDataIdMap.keySet().stream()
                .map(metaDataId -> {
                    TaskMetaData taskMetaData = new TaskMetaData();
                    taskMetaData.setTaskId(taskId);
                    taskMetaData.setMetaDataId(metaDataId);
                    return taskMetaData;
                }).collect(Collectors.toList());

        taskMetaDataService.insert(taskMetaDataList);
        taskMetaDataColumnService.insert(taskMetaDataColumnList);

        //==任务的算力提供者
        List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
        for (TaskPowerSupplier powerSupplier : request.getPowerSupplierList()) {
            TaskPowerProvider powerProvider = new TaskPowerProvider();
            powerProvider.setTaskId(task.getId());
            powerProvider.setIdentityId(powerSupplier.getMemberInfo().getIdentityId());

            powerProvider.setUsedCore(powerSupplier.getPowerInfo().getUsedProcessor());
            powerProvider.setUsedMemory(powerSupplier.getPowerInfo().getUsedMem());
            powerProvider.setUsedBandwidth(powerSupplier.getPowerInfo().getUsedBandwidth());
            taskPowerProviderList.add(powerProvider);
        }
        taskPowerProviderService.insert(taskPowerProviderList);

        //==任务结果接收者
        List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
        for (com.platon.rosettanet.storage.grpc.lib.TaskResultReceiver taskResultReceiver : request.getReceiversList()) {
            for(Organization organization : taskResultReceiver.getProviderList()){
                TaskResultConsumer resultReceiver = new TaskResultConsumer();
                resultReceiver.setTaskId(task.getId());
                resultReceiver.setConsumerIdentityId(taskResultReceiver.getMemberInfo().getIdentityId());
                resultReceiver.setProducerIdentityId(organization.getIdentityId());
                taskResultConsumerList.add(resultReceiver);
            }
        }

        taskResultConsumerService.insert(taskResultConsumerList);

        //接口返回值
        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();

        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查询任务详情（任务ID、节点ID、参与方标识）
     * </pre>
     */
    public void getDetailTask(com.platon.rosettanet.storage.grpc.lib.DetailTaskRequest request,
                              io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.TaskDetail> responseObserver) {

        // 业务代码
        String taskId = request.getTaskId();
        Task task = taskService.findByPK(taskId);

        OrgInfo ownerOrgInfo = orgInfoService.findByPK(task.getOwnerIdentityId());

        List<TaskMetaData> taskMetaDataList = taskMetaDataService.listTaskMetaData(taskId);
        List<TaskPowerProvider> taskPowerProviderList = taskPowerProviderService.listTaskPowerProvider(taskId);
        List<TaskResultConsumer> taskResultConsumerList = taskResultConsumerService.listTaskResultConsumer(taskId);


        TaskDetail taskDetail = TaskDetail.newBuilder()
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setOwner(Organization.newBuilder().setIdentityId(ownerOrgInfo.getIdentityId()).setName(ownerOrgInfo.getOrgName()))
                .setAlgoSupplier(Organization.newBuilder().setIdentityId(task.getOwnerIdentityId()).setName(ownerOrgInfo.getOrgName()))
                .setOperationCost(TaskOperationCostDeclare.newBuilder().setCostProcessor(task.getUsedCore()).setCostMem(task.getUsedMemory()).setCostBandwidth(task.getUsedBandwidth()).build())
                .addAllDataSupplier(convertorService.toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSupplier(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                .build();


        // 返回
        responseObserver.onNext(taskDetail);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查询任务列表
     * </pre>
     */
    public void listTask(com.platon.rosettanet.storage.grpc.lib.TaskListRequest request,
                         io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.TaskListResponse> responseObserver) {
        List<Task> taskList = taskService.listTask();

        List<com.platon.rosettanet.storage.grpc.lib.TaskDetail> grpcTaskList =
                taskList.stream().map(task -> {

                    OrgInfo owner = orgInfoService.findByPK(task.getOwnerIdentityId());

                    List<TaskMetaData> taskMetaDataList = taskMetaDataService.listTaskMetaData(task.getId());

                    List<TaskPowerProvider> taskPowerProviderList = taskPowerProviderService.listTaskPowerProvider(task.getId());

                    List<TaskResultConsumer> taskResultConsumerList = taskResultConsumerService.listTaskResultConsumer(task.getId());

                    return com.platon.rosettanet.storage.grpc.lib.TaskDetail.newBuilder()
                            .setTaskId(task.getId())
                            .setTaskName(task.getTaskName())
                            .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .setOwner(convertorService.toProtoOrganization(owner))
                            .addAllDataSupplier(convertorService.toProtoDataSupplier(taskMetaDataList))
                            .addAllPowerSupplier(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                            .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                            .build();
                }).collect(Collectors.toList());

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskList(grpcTaskList).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查询任务的事件列表
     * </pre>
     */
    public void listTaskEvent(com.platon.rosettanet.storage.grpc.lib.TaskEventRequest request,
                              io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.TaskEventResponse> responseObserver) {

        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(request.getTaskId());

        List<com.platon.rosettanet.storage.grpc.lib.TaskEvent>
                grpcTaskEventList = convertorService.toProtoTaskEvent(taskEventList);

        TaskEventResponse response = TaskEventResponse.newBuilder().addAllTaskEventList(grpcTaskEventList).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
