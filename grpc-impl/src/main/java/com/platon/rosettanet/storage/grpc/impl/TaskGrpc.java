package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.common.exception.OrgNotFound;
import com.platon.rosettanet.storage.common.exception.TaskMetaDataNotFound;
import com.platon.rosettanet.storage.common.exception.TaskResultConsumerNotFound;
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
import org.springframework.util.CollectionUtils;

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

        if(CollectionUtils.isEmpty(request.getDataSupplierList())) {
            throw new TaskMetaDataNotFound();
        }else{
            List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
            List<TaskMetaData> taskMetaDataList = new ArrayList<>();
            for (TaskDataSupplier dataSupplier : request.getDataSupplierList()) {

                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(taskId);
                taskMetaData.setMetaDataId(dataSupplier.getMetaId());
                //冗余
                taskMetaData.setIdentityId(dataSupplier.getMemberInfo().getIdentityId());
                taskMetaData.setPartyId(dataSupplier.getMemberInfo().getPartyId());
                taskMetaDataList.add(taskMetaData);

                if(CollectionUtils.isEmpty(dataSupplier.getColumnMetaList())) {
                    throw new TaskMetaDataNotFound();
                }else {
                    for (MetaDataColumnDetail columnDetail : dataSupplier.getColumnMetaList()) {
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
            }
            taskMetaDataService.insert(taskMetaDataList);
            taskMetaDataColumnService.insert(taskMetaDataColumnList);
        }


        //==任务的算力提供者
        if(!CollectionUtils.isEmpty(request.getPowerSupplierList())) {
            List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (TaskPowerSupplier powerSupplier : request.getPowerSupplierList()) {
                TaskPowerProvider powerProvider = new TaskPowerProvider();
                powerProvider.setTaskId(taskId);
                powerProvider.setIdentityId(powerSupplier.getMemberInfo().getIdentityId());
                powerProvider.setPartyId(powerSupplier.getMemberInfo().getPartyId());
                powerProvider.setUsedCore(powerSupplier.getPowerInfo().getUsedProcessor());
                powerProvider.setUsedMemory(powerSupplier.getPowerInfo().getUsedMem());
                powerProvider.setUsedBandwidth(powerSupplier.getPowerInfo().getUsedBandwidth());
                taskPowerProviderList.add(powerProvider);
            }
            taskPowerProviderService.insert(taskPowerProviderList);
        }

        //==任务结果接收者
        if(CollectionUtils.isEmpty(request.getReceiversList())) {
            throw new TaskResultConsumerNotFound();
        }else{
            List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
            for (com.platon.rosettanet.storage.grpc.lib.TaskResultReceiver taskConsumer : request.getReceiversList()) {
                for(TaskOrganization taskProducer : taskConsumer.getProviderList()){
                    TaskResultConsumer taskResultConsumer = new TaskResultConsumer();
                    taskResultConsumer.setTaskId(taskId);
                    taskResultConsumer.setConsumerIdentityId(taskConsumer.getMemberInfo().getIdentityId());
                    taskResultConsumer.setConsumerPartyId(taskConsumer.getMemberInfo().getPartyId());
                    taskResultConsumer.setProducerIdentityId(taskProducer.getIdentityId());
                    taskResultConsumer.setProducerPartyId(taskProducer.getPartyId());
                    taskResultConsumerList.add(taskResultConsumer);
                }
            }
            taskResultConsumerService.insert(taskResultConsumerList);
        }


        //==任务日志
        if(!CollectionUtils.isEmpty(request.getTaskEventListList())) {
            List<TaskEvent> taskEventList = new ArrayList<>();
            for (com.platon.rosettanet.storage.grpc.lib.TaskEvent event : request.getTaskEventListList()) {
                TaskEvent taskEvent = new TaskEvent();
                taskEvent.setTaskId(taskId);
                taskEvent.setEventAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getCreateAt()), ZoneId.systemDefault()));
                taskEvent.setEventType(event.getType());
                taskEvent.setIdentityId(event.getOwner().getIdentityId());
                taskEvent.setEventContent(event.getContent());
                taskEventList.add(taskEvent);
            }
            taskEventService.insert(taskEventList);
        }

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

        TaskDetail taskDetail = toTaskDetail(task);

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

        List<com.platon.rosettanet.storage.grpc.lib.TaskDetail> grpcTaskList = toTaskDetailList(taskList);

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskList(grpcTaskList).build();

        log.debug("listTask, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<com.platon.rosettanet.storage.grpc.lib.TaskDetail> toTaskDetailList(List<Task> taskList){
        List<com.platon.rosettanet.storage.grpc.lib.TaskDetail> grpcTaskList =
                taskList.parallelStream().map(task -> {
                    return toTaskDetail(task);
                }).collect(Collectors.toList());
        return grpcTaskList;
    }

    private com.platon.rosettanet.storage.grpc.lib.TaskDetail toTaskDetail(Task task){
        OrgInfo owner = orgInfoService.findByPK(task.getOwnerIdentityId());
        if(owner==null){
            log.error("task owner identity id not found. taskId:={}, identityId:={}", task.getId(), task.getOwnerIdentityId());
            throw new OrgNotFound();
        }

        TaskAlgoProvider taskAlgoProvider = taskAlgoProviderService.findAlgoProviderByTaskId(task.getId());
        OrgInfo taskAlgoProviderOrgInfo = orgInfoService.findByPK(taskAlgoProvider.getIdentityId());

        List<TaskMetaData> taskMetaDataList = taskMetaDataService.listTaskMetaData(task.getId());
        if(CollectionUtils.isEmpty(taskMetaDataList)){
            log.error("task metadata not found. taskId:={}", task.getId());
            throw new TaskMetaDataNotFound();
        }

        List<TaskPowerProvider> taskPowerProviderList = taskPowerProviderService.listTaskPowerProvider(task.getId());
        //task 可以没有power
        /*if(CollectionUtils.isEmpty(taskPowerProviderList)){
            log.error("task power not found. taskId:={}", task.getId());
            throw new TaskPowerNotFound();
        }*/

        List<TaskResultConsumer> taskResultConsumerList = taskResultConsumerService.listTaskResultConsumer(task.getId());

        return com.platon.rosettanet.storage.grpc.lib.TaskDetail.newBuilder()
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(task.getStatus())
                .setOwner(convertorService.toProtoTaskOrganization(owner, task.getOwnerPartyId()))
                .setAlgoSupplier(convertorService.toProtoTaskOrganization(taskAlgoProviderOrgInfo, taskAlgoProvider.getPartyId()))
                .setOperationCost(TaskOperationCostDeclare.newBuilder().setCostProcessor(task.getRequiredCore()).setCostMem(task.getRequiredMemory()).setCostBandwidth(task.getRequiredBandwidth()).setDuration(task.getRequiredDuration()).build())
                .addAllDataSupplier(convertorService.toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSupplier(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                .build();
    }

    public void listTaskByIdentity(com.platon.rosettanet.storage.grpc.lib.TaskListByIdentityRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.TaskListResponse> responseObserver) {
        log.debug("listTaskByIdentity, request:{}", request);

        List<Task> taskList = taskService.listTaskByIdentityId(request.getIdentityId());

        List<com.platon.rosettanet.storage.grpc.lib.TaskDetail> grpcTaskList = toTaskDetailList(taskList);

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskList(grpcTaskList).build();

        log.debug("listTaskByIdentity, response:{}", response);
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
