package com.platon.rosettanet.storage.grpc.impl;

import com.platon.rosettanet.storage.common.exception.OrgNotFound;
import com.platon.rosettanet.storage.common.exception.TaskMetaDataNotFound;
import com.platon.rosettanet.storage.common.exception.TaskResultConsumerNotFound;
import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.api.TaskEventResponse;
import com.platon.rosettanet.storage.grpc.lib.api.TaskListResponse;
import com.platon.rosettanet.storage.grpc.lib.api.TaskServiceGrpc;
import com.platon.rosettanet.storage.grpc.lib.common.SimpleResponse;
import com.platon.rosettanet.storage.grpc.lib.common.TaskOrganization;
import com.platon.rosettanet.storage.grpc.lib.common.TaskState;
import com.platon.rosettanet.storage.grpc.lib.types.MetadataColumn;
import com.platon.rosettanet.storage.grpc.lib.types.TaskDataSupplier;
import com.platon.rosettanet.storage.grpc.lib.types.TaskDetail;
import com.platon.rosettanet.storage.grpc.lib.types.TaskPowerSupplier;
import com.platon.rosettanet.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class TaskGrpc extends TaskServiceGrpc.TaskServiceImplBase {

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
    public void saveTask(TaskDetail request,
                         io.grpc.stub.StreamObserver<SimpleResponse> responseObserver) {

        log.debug("saveTask, request:{}", request);

        // 业务代码

        String taskId = request.getTaskId();

        //==任务
        Task task = new Task();

        task.setId(taskId);
        //任务名称
        task.setTaskName(request.getTaskName());

        //任务所有人
        task.setOwnerIdentityId(request.getSender().getIdentityId());
        task.setOwnerPartyId(request.getSender().getPartyId());

        task.setUserId(request.getUser());
        task.setUserType(request.getUserType().ordinal());

        //所需资源
        task.setRequiredBandwidth(request.getOperationCost().getBandwidth());
        task.setRequiredCore(request.getOperationCost().getProcessor());
        task.setRequiredMemory(request.getOperationCost().getMemory());
        task.setRequiredDuration(request.getOperationCost().getDuration());

        task.setUsedBandwidth(request.getOperationCost().getBandwidth());
        task.setUsedCore(request.getOperationCost().getProcessor());
        task.setUsedMemory(request.getOperationCost().getMemory());

        //创建时间
        task.setCreateAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getCreateAt()), ZoneOffset.UTC));
        //开始时间
        task.setStartAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartAt()), ZoneOffset.UTC));
        //结束时间
        task.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getEndAt()), ZoneOffset.UTC));

        //任务状态
        task.setStatus(request.getState().ordinal());

        taskService.insert(task);

        //==算法提供者
        TaskAlgoProvider taskAlgoProvider = new TaskAlgoProvider();
        taskAlgoProvider.setTaskId(taskId);
        taskAlgoProvider.setIdentityId(request.getAlgoSupplier().getIdentityId());
        taskAlgoProvider.setPartyId(request.getAlgoSupplier().getPartyId());
        taskAlgoProviderService.insert(taskAlgoProvider);

        //==任务的数据提供者

        if(CollectionUtils.isEmpty(request.getDataSuppliersList())) {
            throw new TaskMetaDataNotFound();
        }else{
            List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
            List<TaskMetaData> taskMetaDataList = new ArrayList<>();
            for (TaskDataSupplier dataSupplier : request.getDataSuppliersList()) {

                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(taskId);
                taskMetaData.setMetaDataId(dataSupplier.getMetadataId());
                //冗余
                taskMetaData.setIdentityId(dataSupplier.getOrganization().getIdentityId());
                taskMetaData.setPartyId(dataSupplier.getOrganization().getPartyId());
                taskMetaDataList.add(taskMetaData);

                if(CollectionUtils.isEmpty(dataSupplier.getColumnsList())) {
                    throw new TaskMetaDataNotFound();
                }else {
                    for (MetadataColumn columnDetail : dataSupplier.getColumnsList()) {
                        TaskMetaDataColumn dataProvider = new TaskMetaDataColumn();
                        dataProvider.setTaskId(taskId);

                        String metaId = dataSupplier.getMetadataId();
                        int cindex = columnDetail.getCIndex();

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
        if(!CollectionUtils.isEmpty(request.getPowerSuppliersList())) {
            List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (TaskPowerSupplier powerSupplier : request.getPowerSuppliersList()) {
                TaskPowerProvider powerProvider = new TaskPowerProvider();
                powerProvider.setTaskId(taskId);
                powerProvider.setIdentityId(powerSupplier.getOrganization().getIdentityId());
                powerProvider.setPartyId(powerSupplier.getOrganization().getPartyId());
                powerProvider.setUsedCore(powerSupplier.getResourceUsedOverview().getUsedProcessor());
                powerProvider.setUsedMemory(powerSupplier.getResourceUsedOverview().getUsedMem());
                powerProvider.setUsedBandwidth(powerSupplier.getResourceUsedOverview().getUsedBandwidth());
                taskPowerProviderList.add(powerProvider);
            }
            taskPowerProviderService.insert(taskPowerProviderList);
        }

        //==任务结果接收者
        if(CollectionUtils.isEmpty(request.getReceiversList())) {
            throw new TaskResultConsumerNotFound();
        }else{
            List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
            for (TaskOrganization taskConsumer : request.getReceiversList()) {
                TaskResultConsumer taskResultConsumer = new TaskResultConsumer();
                taskResultConsumer.setTaskId(taskId);
                taskResultConsumer.setConsumerIdentityId(taskConsumer.getIdentityId());
                taskResultConsumer.setConsumerPartyId(taskConsumer.getPartyId());
                taskResultConsumerList.add(taskResultConsumer);
            }
            taskResultConsumerService.insert(taskResultConsumerList);
        }


        //==任务日志
        if(!CollectionUtils.isEmpty(request.getTaskEventsList())) {
            List<TaskEvent> taskEventList = new ArrayList<>();
            for (com.platon.rosettanet.storage.grpc.lib.types.TaskEvent event : request.getTaskEventsList()) {
                TaskEvent taskEvent = new TaskEvent();
                taskEvent.setTaskId(taskId);
                taskEvent.setEventAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getCreateAt()), ZoneOffset.UTC));
                taskEvent.setEventType(event.getType());
                taskEvent.setIdentityId(event.getIdentityId());
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
    public void getDetailTask(com.platon.rosettanet.storage.grpc.lib.api.DetailTaskRequest request,
                              io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.types.TaskDetail> responseObserver) {

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
    public void listTask(com.platon.rosettanet.storage.grpc.lib.api.TaskListRequest request,
                         io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.TaskListResponse> responseObserver) {

        log.debug("listTask, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<Task> taskList = taskService.syncTask(lastUpdateAt);

        List<com.platon.rosettanet.storage.grpc.lib.types.TaskDetail> grpcTaskList = toTaskDetailList(taskList);

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskDetails(grpcTaskList).build();

        log.debug("listTask, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<com.platon.rosettanet.storage.grpc.lib.types.TaskDetail> toTaskDetailList(List<Task> taskList){
        List<com.platon.rosettanet.storage.grpc.lib.types.TaskDetail> grpcTaskList =
                taskList.parallelStream().map(task -> {
                    return toTaskDetail(task);
                }).collect(Collectors.toList());
        return grpcTaskList;
    }

    private com.platon.rosettanet.storage.grpc.lib.types.TaskDetail toTaskDetail(Task task){
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

        return com.platon.rosettanet.storage.grpc.lib.types.TaskDetail.newBuilder()
                .setTaskId(task.getId())
                .setTaskName(task.getTaskName())
                .setCreateAt(task.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setStartAt(task.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setEndAt(task.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setState(TaskState.forNumber(task.getStatus())) //todo: to check is right
                .setSender(convertorService.toProtoTaskOrganization(owner, task.getOwnerPartyId()))
                .setAlgoSupplier(convertorService.toProtoTaskOrganization(taskAlgoProviderOrgInfo, taskAlgoProvider.getPartyId()))
                .setOperationCost(com.platon.rosettanet.storage.grpc.lib.common.TaskResourceCostDeclare.newBuilder().setProcessor(task.getRequiredCore()).setMemory(task.getRequiredMemory()).setBandwidth(task.getRequiredBandwidth()).setDuration(task.getRequiredDuration()).build())
                .addAllDataSuppliers(convertorService.toProtoDataSupplier(taskMetaDataList))
                .addAllPowerSuppliers(convertorService.toProtoPowerSupplier(taskPowerProviderList))
                .addAllReceivers(convertorService.toProtoResultReceiver(taskResultConsumerList))
                .build();
    }

    public void listTaskByIdentity(com.platon.rosettanet.storage.grpc.lib.api.TaskListByIdentityRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.TaskListResponse> responseObserver) {
        log.debug("listTaskByIdentity, request:{}", request);

        List<Task> taskList = taskService.listTaskByIdentityId(request.getIdentityId());

        List<com.platon.rosettanet.storage.grpc.lib.types.TaskDetail> grpcTaskList = toTaskDetailList(taskList);

        TaskListResponse response = TaskListResponse.newBuilder().addAllTaskDetails(grpcTaskList).build();

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
    public void listTaskEvent(com.platon.rosettanet.storage.grpc.lib.api.TaskEventRequest request,
                              io.grpc.stub.StreamObserver<com.platon.rosettanet.storage.grpc.lib.api.TaskEventResponse> responseObserver) {
        log.debug("listTaskEvent, request:{}", request);

        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(request.getTaskId());

        List<com.platon.rosettanet.storage.grpc.lib.types.TaskEvent>
                grpcTaskEventList = convertorService.toProtoTaskEvent(taskEventList);

        TaskEventResponse response = TaskEventResponse.newBuilder().addAllTaskEvents(grpcTaskEventList).build();

        log.debug("listTaskEvent, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
