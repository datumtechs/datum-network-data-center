package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.common.exception.TaskMetaDataNotFound;
import com.platon.metis.storage.common.exception.TaskResultConsumerNotFound;
import com.platon.metis.storage.dao.entity.Task;
import com.platon.metis.storage.dao.entity.*;
import com.platon.metis.storage.grpc.lib.api.*;
import com.platon.metis.storage.grpc.lib.common.SimpleResponse;
import com.platon.metis.storage.grpc.lib.common.TaskOrganization;
import com.platon.metis.storage.grpc.lib.types.MetadataColumn;
import com.platon.metis.storage.grpc.lib.types.TaskDataSupplier;
import com.platon.metis.storage.grpc.lib.types.TaskPB;
import com.platon.metis.storage.grpc.lib.types.TaskPowerSupplier;
import com.platon.metis.storage.service.*;
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
    public void saveTask(SaveTaskRequest request,
                         io.grpc.stub.StreamObserver<SimpleResponse> responseObserver) {

        log.debug("saveTask, request:{}", request);

        // 业务代码

        String taskId = request.getTask().getTaskId();

        //==任务
        Task task = new Task();

        task.setId(taskId);
        //任务名称
        task.setTaskName(request.getTask().getTaskName());

        //任务所有人
        task.setOwnerIdentityId(request.getTask().getIdentityId());
        task.setOwnerPartyId(request.getTask().getPartyId());

        task.setUserId(request.getTask().getUser());
        task.setUserType(request.getTask().getUserType().ordinal());

        //所需资源
        task.setRequiredBandwidth(request.getTask().getOperationCost().getBandwidth());
        task.setRequiredCore(request.getTask().getOperationCost().getProcessor());
        task.setRequiredMemory(request.getTask().getOperationCost().getMemory());
        task.setRequiredDuration(request.getTask().getOperationCost().getDuration());

        task.setUsedBandwidth(request.getTask().getOperationCost().getBandwidth());
        task.setUsedCore(request.getTask().getOperationCost().getProcessor());
        task.setUsedMemory(request.getTask().getOperationCost().getMemory());

        //创建时间
        task.setCreateAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTask().getCreateAt()), ZoneOffset.UTC));
        //开始时间
        task.setStartAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTask().getStartAt()), ZoneOffset.UTC));
        //结束时间
        task.setEndAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTask().getEndAt()), ZoneOffset.UTC));

        //任务状态
        task.setStatus(request.getTask().getState().ordinal());

        taskService.insert(task);

        //==算法提供者
        TaskAlgoProvider taskAlgoProvider = new TaskAlgoProvider();
        taskAlgoProvider.setTaskId(taskId);
        taskAlgoProvider.setIdentityId(request.getTask().getAlgoSupplier().getIdentityId());
        taskAlgoProvider.setPartyId(request.getTask().getAlgoSupplier().getPartyId());
        taskAlgoProviderService.insert(taskAlgoProvider);

        //==任务的数据提供者

        if(CollectionUtils.isEmpty(request.getTask().getDataSuppliersList())) {
            throw new TaskMetaDataNotFound();
        }else{
            List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
            List<TaskMetaData> taskMetaDataList = new ArrayList<>();
            for (TaskDataSupplier dataSupplier : request.getTask().getDataSuppliersList()) {

                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(taskId);
                taskMetaData.setMetaDataId(dataSupplier.getMetadataId());
                //冗余
                taskMetaData.setIdentityId(dataSupplier.getOrganization().getIdentityId());
                taskMetaData.setPartyId(dataSupplier.getOrganization().getPartyId());
                taskMetaData.setKeyColumnIdx(dataSupplier.getKeyColumn().getCIndex());
                taskMetaDataList.add(taskMetaData);

                if(CollectionUtils.isEmpty(dataSupplier.getSelectedColumnsList())) {
                    throw new TaskMetaDataNotFound();
                }else {
                    for (MetadataColumn selectedColumn : dataSupplier.getSelectedColumnsList()) {
                        TaskMetaDataColumn taskMetaDataColumn = new TaskMetaDataColumn();
                        taskMetaDataColumn.setTaskId(taskId);

                        String metaId = dataSupplier.getMetadataId();
                        int selectedColumnIdx = selectedColumn.getCIndex();

                        //元数据校验
                       /*todo:
                       List<MetaDataColumn> metaDataColumns = metaDataService.listMetaDataColumn(metaId);
                        Optional<MetaDataColumn> first = metaDataColumns.stream().filter(metaDataColumn -> {
                            return cindex == metaDataColumn.getColumnIdx();
                        }).findFirst();
                        first.orElseThrow(() -> new RuntimeException("元数据校验失败,metaId=" + metaId + ",cindex=" + cindex));
                        */

                        taskMetaDataColumn.setMetaDataId(metaId);
                        taskMetaDataColumn.setSelectedColumnIdx(selectedColumnIdx);
                        taskMetaDataColumnList.add(taskMetaDataColumn);
                    }
                }
            }
            taskMetaDataService.insert(taskMetaDataList);
            taskMetaDataColumnService.insert(taskMetaDataColumnList);
        }


        //==任务的算力提供者
        if(!CollectionUtils.isEmpty(request.getTask().getPowerSuppliersList())) {
            List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (TaskPowerSupplier powerSupplier : request.getTask().getPowerSuppliersList()) {
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
        if(CollectionUtils.isEmpty(request.getTask().getReceiversList())) {
            throw new TaskResultConsumerNotFound();
        }else{
            List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
            for (TaskOrganization taskConsumer : request.getTask().getReceiversList()) {
                TaskResultConsumer taskResultConsumer = new TaskResultConsumer();
                taskResultConsumer.setTaskId(taskId);
                taskResultConsumer.setConsumerIdentityId(taskConsumer.getIdentityId());
                taskResultConsumer.setConsumerPartyId(taskConsumer.getPartyId());
                taskResultConsumerList.add(taskResultConsumer);
            }
            taskResultConsumerService.insert(taskResultConsumerList);
        }


        //==任务日志
        if(!CollectionUtils.isEmpty(request.getTask().getTaskEventsList())) {
            List<TaskEvent> taskEventList = new ArrayList<>();
            for (com.platon.metis.storage.grpc.lib.types.TaskEvent event : request.getTask().getTaskEventsList()) {
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
    public void getTaskDetail(com.platon.metis.storage.grpc.lib.api.GetTaskDetailRequest request,
                              io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.GetTaskDetailResponse> responseObserver) {

        log.debug("getDetailTask, request:{}", request);

        // 业务代码
        String taskId = request.getTaskId();
        Task task = taskService.findByPK(taskId);

        TaskPB taskPB = convertorService.toTaskPB(task);

        log.debug("getTaskDetail, taskDetail:{}", taskPB);

        GetTaskDetailResponse response = GetTaskDetailResponse.newBuilder().setTask(taskPB).build();
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * <pre>
     * 查询任务列表
     * </pre>
     */
    public void listTask(com.platon.metis.storage.grpc.lib.api.ListTaskRequest request,
                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskResponse> responseObserver) {

        log.debug("listTask, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastUpdated()), ZoneOffset.UTC);
        }

        List<Task> taskList = taskService.syncTask(lastUpdateAt);

        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList = convertorService.toTaskPB(taskList);

        ListTaskResponse response = ListTaskResponse.newBuilder().addAllTasks(grpcTaskList).build();

        log.debug("listTask, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



    public void listTaskByIdentity(com.platon.metis.storage.grpc.lib.api.ListTaskByIdentityRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskResponse> responseObserver) {
        log.debug("listTaskByIdentity, request:{}", request);

        List<Task> taskList = taskService.listTaskByIdentityId(request.getIdentityId());

        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList = convertorService.toTaskPB(taskList);

        ListTaskResponse response = ListTaskResponse.newBuilder().addAllTasks(grpcTaskList).build();

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
    public void listTaskEvent(com.platon.metis.storage.grpc.lib.api.ListTaskEventRequest request,
                              io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskEventResponse> responseObserver) {
        log.debug("listTaskEvent, request:{}", request);

        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(request.getTaskId());

        List<com.platon.metis.storage.grpc.lib.types.TaskEvent>
                grpcTaskEventList = convertorService.toProtoTaskEvent(taskEventList);

        ListTaskEventResponse response = ListTaskEventResponse.newBuilder().addAllTaskEvents(grpcTaskEventList).build();

        log.debug("listTaskEvent, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
