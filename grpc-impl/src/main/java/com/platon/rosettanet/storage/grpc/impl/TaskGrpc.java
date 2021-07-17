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
import java.util.List;
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

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private TaskAlgoProviderService taskAlgoProviderService;
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

        log.debug("saveTask, request:{}", request);

        // 业务代码

        String taskId = request.getTaskId();

        //==任务
        Task task = new Task();

        task.setId(taskId);
        //任务名称
        task.setTaskName(request.getTaskName());

        //任务所有人
        task.setOwnerIdentityId(request.getOwner().getIdentityId());
        task.setOwnerPartyId(request.getOwner().getPartyId());

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
        //开始时间
        task.setStartAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartAt()), ZoneId.systemDefault()));
        //结束时间
        task.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getEndAt()), ZoneId.systemDefault()));

        //任务状态
        task.setStatus(request.getState());

        taskService.insert(task);

        //==算法提供者
        TaskAlgoProvider taskAlgoProvider = new TaskAlgoProvider();
        taskAlgoProvider.setTaskId(taskId);
        taskAlgoProvider.setIdentityId(request.getAlgoSupplier().getIdentityId());
        taskAlgoProvider.setPartyId(request.getAlgoSupplier().getPartyId());
        taskAlgoProviderService.insert(taskAlgoProvider);

        //==任务的数据提供者
        List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
        List<TaskMetaData> taskMetaDataList = new ArrayList<>();
        for (TaskDataSupplier dataSupplier : request.getDataSupplierList()) {

            TaskMetaData taskMetaData = new TaskMetaData();
            taskMetaData.setTaskId(taskId);
            taskMetaData.setMetaDataId(dataSupplier.getMetaId());
            taskMetaData.setPartyId(dataSupplier.getMemberInfo().getPartyId());
            taskMetaDataList.add(taskMetaData);

            for(MetaDataColumnDetail columnDetail : dataSupplier.getColumnMetaList()){
                TaskMetaDataColumn dataProvider = new TaskMetaDataColumn();
                dataProvider.setTaskId(taskId);

                String metaId = dataSupplier.getMetaId();
                int cindex = columnDetail.getCindex();

                //元数据校验
               /*
               List<MetaDataColumn> metaDataColumns = metaDataService.listMetaDataColumn(metaId);
                Optional<MetaDataColumn> first = metaDataColumns.stream().filter(metaDataColumn -> {
                    return cindex == metaDataColumn.getColumnIdx();
                }).findFirst();
                first.orElseThrow(() -> new RuntimeException("元数据校验失败,metaId=" + metaId + ",cindex=" + cindex));
                */


                dataProvider.setMetaDataId(metaId);
                dataProvider.setColumnIdx(cindex);
                taskMetaDataColumnList.add(dataProvider);
            }
        }

        taskMetaDataService.insert(taskMetaDataList);
        taskMetaDataColumnService.insert(taskMetaDataColumnList);

        //==任务的算力提供者
        List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
        for (TaskPowerSupplier powerSupplier : request.getPowerSupplierList()) {
            TaskPowerProvider powerProvider = new TaskPowerProvider();
            powerProvider.setTaskId(task.getId());
            powerProvider.setIdentityId(powerSupplier.getMemberInfo().getIdentityId());
            powerProvider.setPartyId(powerSupplier.getMemberInfo().getPartyId());
            powerProvider.setUsedCore(powerSupplier.getPowerInfo().getUsedProcessor());
            powerProvider.setUsedMemory(powerSupplier.getPowerInfo().getUsedMem());
            powerProvider.setUsedBandwidth(powerSupplier.getPowerInfo().getUsedBandwidth());
            taskPowerProviderList.add(powerProvider);
        }
        taskPowerProviderService.insert(taskPowerProviderList);

        //==任务结果接收者
        List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
        for (com.platon.rosettanet.storage.grpc.lib.TaskResultReceiver taskConsumer : request.getReceiversList()) {
            for(TaskOrganization taskProducer : taskConsumer.getProviderList()){
                TaskResultConsumer taskResultConsumer = new TaskResultConsumer();
                taskResultConsumer.setTaskId(task.getId());
                taskResultConsumer.setConsumerIdentityId(taskConsumer.getMemberInfo().getIdentityId());
                taskResultConsumer.setConsumerPartyId(taskConsumer.getMemberInfo().getPartyId());
                taskResultConsumer.setProducerIdentityId(taskProducer.getIdentityId());
                taskResultConsumer.setProducerPartyId(taskProducer.getPartyId());
                taskResultConsumerList.add(taskResultConsumer);
            }
        }

        taskResultConsumerService.insert(taskResultConsumerList);

        //接口返回值
        SimpleResponse response = SimpleResponse.newBuilder().setStatus(0).build();


        log.debug("saveTask, response:{}", response);
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

        log.debug("getDetailTask, request:{}", request);

        // 业务代码
        String taskId = request.getTaskId();
        Task task = taskService.findByPK(taskId);

        OrgInfo ownerOrgInfo = orgInfoService.findByPK(task.getOwnerIdentityId());

        TaskAlgoProvider taskAlgoProvider = taskAlgoProviderService.findAlgoProviderByTaskId(taskId);

        List<TaskMetaData> taskMetaDataList = taskMetaDataService.listTaskMetaData(taskId);
        List<TaskPowerProvider> taskPowerProviderList = taskPowerProviderService.listTaskPowerProvider(taskId);
        List<TaskResultConsumer> taskResultConsumerList = taskResultConsumerService.listTaskResultConsumer(taskId);


        TaskDetail taskDetail = TaskDetail.newBuilder()
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setOwner(TaskOrganization.newBuilder().setIdentityId(ownerOrgInfo.getIdentityId()).setPartyId(task.getOwnerPartyId()).setName(ownerOrgInfo.getOrgName()))
                .setAlgoSupplier(TaskOrganization.newBuilder().setIdentityId(taskAlgoProvider.getIdentityId()).setPartyId(taskAlgoProvider.getPartyId()))
                .setOperationCost(TaskOperationCostDeclare.newBuilder().setCostProcessor(task.getUsedCore()).setCostMem(task.getUsedMemory()).setCostBandwidth(task.getUsedBandwidth()).build())
                .addAllDataSupplier(convertorService.toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSupplier(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                .build();

        log.debug("getDetailTask, taskDetail:{}", taskDetail);

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

        log.debug("listTask, request:{}", request);

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
                            .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .setOwner(convertorService.toProtoTaskOrganization(owner, task.getOwnerPartyId()))
                            .addAllDataSupplier(convertorService.toProtoDataSupplier(taskMetaDataList))
                            .addAllPowerSupplier(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                            .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                            .build();
                }).collect(Collectors.toList());

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskList(grpcTaskList).build();

        log.debug("listTask, response:{}", response);
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
        log.debug("listTaskEvent, request:{}", request);

        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(request.getTaskId());

        List<com.platon.rosettanet.storage.grpc.lib.TaskEvent>
                grpcTaskEventList = convertorService.toProtoTaskEvent(taskEventList);

        TaskEventResponse response = TaskEventResponse.newBuilder().addAllTaskEventList(grpcTaskEventList).build();

        log.debug("listTaskEvent, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
