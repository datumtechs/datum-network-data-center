package com.platon.metis.storage.grpc.impl;

import com.platon.metis.storage.common.util.LocalDateTimeUtil;
import com.platon.metis.storage.dao.entity.TaskEvent;
import com.platon.metis.storage.dao.entity.TaskInfo;
import com.platon.metis.storage.dao.entity.TaskOrg;
import com.platon.metis.storage.dao.entity.TaskOrg.TaskRoleEnum;
import com.platon.metis.storage.dao.entity.TaskPowerResourceOption;
import com.platon.metis.storage.grpc.lib.api.GetTaskDetailResponse;
import com.platon.metis.storage.grpc.lib.api.ListTaskEventResponse;
import com.platon.metis.storage.grpc.lib.api.ListTaskResponse;
import com.platon.metis.storage.grpc.lib.api.TaskServiceGrpc;
import com.platon.metis.storage.grpc.lib.types.Base;
import com.platon.metis.storage.grpc.lib.types.ResourceUsageOverview;
import com.platon.metis.storage.grpc.lib.types.TaskPB;
import com.platon.metis.storage.service.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@Service
public class TaskGrpc extends TaskServiceGrpc.TaskServiceImplBase {

    @Autowired
    private TaskEventService taskEventService;

    @Autowired
    private ConvertorService convertorService;

    @Resource
    private TaskInfoService taskInfoService;

    @Resource
    private TaskOrgService taskOrgService;

    @Resource
    private TaskDataFlowOptionPartService taskDataFlowOptionPartService;

    @Resource
    private TaskDataOptionPartService taskDataOptionPartService;

    @Resource
    private TaskInnerAlgorithmCodePartService taskInnerAlgorithmCodePartService;

    @Resource
    private TaskPowerOptionPartService taskPowerOptionPartService;

    @Resource
    private TaskPowerResourceOptionService taskPowerResourceOptionService;

    @Resource
    private TaskReceiverOptionService taskReceiverOptionService;


    /**
     * <pre>
     * 存储任务
     * </pre>
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    @Override
    public void saveTask(com.platon.metis.storage.grpc.lib.api.SaveTaskRequest request,
                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.types.Base.SimpleResponse> responseObserver) {

        log.debug("saveTask, request:{}", request);

        TaskPB taskPB = request.getTask();
        //task的请求内容
        String taskId = taskPB.getTaskId();

        //task的基本信息
        TaskInfo taskInfo = toTaskInfo(taskPB);
        taskInfoService.saveTask(taskInfo);

        //参与任务的组织信息
        List<TaskOrg> taskOrgList = toTaskOrgList(taskPB);
        taskOrgService.saveTaskOrg(taskOrgList);

        //任务涉及的大字段属性单独存储
        taskDataFlowOptionPartService.saveDataFlowOption(taskId, taskPB.getDataFlowPolicyOptionsList());
        taskDataOptionPartService.saveDataOption(taskId, taskPB.getDataPolicyOptionsList());
        taskInnerAlgorithmCodePartService.saveAlgorithmCode(taskId, taskPB.getAlgorithmCode(), taskPB.getAlgorithmCodeExtraParams());
        taskPowerOptionPartService.savePowerOption(taskId, taskPB.getPowerPolicyOptionsList());
        taskPowerResourceOptionService.savePowerResourceOption(toPowerResourceOption(taskPB));
        taskReceiverOptionService.saveReceiverOption(taskId, taskPB.getReceiverPolicyOptionsList());

        //==任务日志
        List<TaskEvent> taskEvents = toTaskEventList(taskPB);
        if (!CollectionUtils.isEmpty(taskEvents)) {
            taskEventService.insert(taskEvents);
        }

        //接口返回值
        Base.SimpleResponse response = Base.SimpleResponse.newBuilder().setStatus(0).build();

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
    @Override
    public void getTaskDetail(com.platon.metis.storage.grpc.lib.api.GetTaskDetailRequest request,
                              io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.GetTaskDetailResponse> responseObserver) {

        log.debug("getDetailTask, request:{}", request);

        // 业务代码
        String taskId = request.getTaskId();
        TaskInfo taskInfo = taskInfoService.findByTaskId(taskId);
        TaskPB taskPB = convertorService.toTaskPB(taskInfo);
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
    @Override
    public void listTask(com.platon.metis.storage.grpc.lib.api.ListTaskRequest request,
                         io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskResponse> responseObserver) {

        log.debug("listTask, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<TaskInfo> taskInfoList = taskInfoService.syncTaskInfo(lastUpdateAt, request.getPageSize());

        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);

        ListTaskResponse response = ListTaskResponse.newBuilder().addAllTasks(grpcTaskList).build();

        log.debug("listTask, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void listTaskByIdentity(com.platon.metis.storage.grpc.lib.api.ListTaskByIdentityRequest request,
                                   io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskResponse> responseObserver) {
        log.debug("listTaskByIdentity, request:{}", request);

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<TaskInfo> taskInfoList = taskInfoService.listTaskInfoByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);


        ListTaskResponse response = ListTaskResponse.newBuilder().addAllTasks(grpcTaskList).build();

        log.debug("listTaskByIdentity, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    /**
     * <pre>
     * 根据任务Ids查询任务列表 (v3.0)
     * </pre>
     */
    @Override
    public void listTaskByTaskIds(com.platon.metis.storage.grpc.lib.api.ListTaskByTaskIdsRequest request,
                                  io.grpc.stub.StreamObserver<com.platon.metis.storage.grpc.lib.api.ListTaskResponse> responseObserver) {

        log.debug("listTaskByTaskIds, request:{}", request);

        List<TaskInfo> taskInfoList = taskInfoService.listTaskInfoByTaskIds(request.getTaskIdsList());

        List<com.platon.metis.storage.grpc.lib.types.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);

        ListTaskResponse response = ListTaskResponse.newBuilder().addAllTasks(grpcTaskList).build();

        log.debug("listTaskByTaskIds, response:{}", response);
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * 查询任务的事件列表
     * </pre>
     */
    @Override
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

    private List<TaskEvent> toTaskEventList(TaskPB taskPB) {
        List<TaskEvent> taskEventList = new ArrayList<>();
        for (com.platon.metis.storage.grpc.lib.types.TaskEvent event : taskPB.getTaskEventsList()) {
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.setTaskId(taskPB.getTaskId());
            taskEvent.setEventAt(LocalDateTimeUtil.getLocalDateTme(event.getCreateAt()));
            taskEvent.setEventType(event.getType());
            taskEvent.setIdentityId(event.getIdentityId());
            taskEvent.setPartyId(event.getPartyId());
            taskEvent.setEventContent(event.getContent());
            taskEventList.add(taskEvent);
        }
        return taskEventList;
    }

    private List<TaskPowerResourceOption> toPowerResourceOption(TaskPB taskPB) {
        List<TaskPowerResourceOption> list = taskPB.getPowerResourceOptionsList().stream()
                .map(taskPowerResourceOption -> {
                    TaskPowerResourceOption option = new TaskPowerResourceOption();
                    option.setTaskId(taskPB.getTaskId());
                    option.setPartId(taskPowerResourceOption.getPartyId());
                    ResourceUsageOverview resourceUsedOverview = taskPowerResourceOption.getResourceUsedOverview();
                    option.setTotalMemory(resourceUsedOverview.getTotalMem());
                    option.setUsedMemory(resourceUsedOverview.getUsedMem());
                    option.setTotalProcessor(resourceUsedOverview.getTotalProcessor());
                    option.setUsedProcessor(resourceUsedOverview.getUsedProcessor());
                    option.setTotalBandwidth(resourceUsedOverview.getTotalBandwidth());
                    option.setUsedBandwidth(resourceUsedOverview.getUsedBandwidth());
                    option.setTotalDisk(resourceUsedOverview.getTotalDisk());
                    option.setUsedDisk(resourceUsedOverview.getUsedDisk());
                    return option;
                }).collect(Collectors.toList());
        return list;
    }

    private List<TaskOrg> toTaskOrgList(TaskPB taskPB) {
        String taskId = taskPB.getTaskId();
        TaskOrg senderOrg = toTaskOrg(taskId, TaskRoleEnum.sender, taskPB.getSender());
        TaskOrg algoSupplierOrg = toTaskOrg(taskId, TaskRoleEnum.algoSupplier, taskPB.getAlgoSupplier());
        List<TaskOrg> dataSupplierOrgList = taskPB.getDataSuppliersList().stream()
                .map(dataSupplier -> toTaskOrg(taskId, TaskRoleEnum.dataSupplier, dataSupplier))
                .collect(Collectors.toList());
        List<TaskOrg> powerSupplierOrgList = taskPB.getPowerSuppliersList().stream()
                .map(powerSupplier -> toTaskOrg(taskId, TaskRoleEnum.powerSupplier, powerSupplier))
                .collect(Collectors.toList());
        List<TaskOrg> receiverOrgList = taskPB.getReceiversList().stream()
                .map(receiver -> toTaskOrg(taskId, TaskRoleEnum.receiver, receiver))
                .collect(Collectors.toList());
        List<TaskOrg> taskOrgList = new ArrayList<>();
        taskOrgList.add(senderOrg);
        taskOrgList.add(algoSupplierOrg);
        taskOrgList.addAll(dataSupplierOrgList);
        taskOrgList.addAll(powerSupplierOrgList);
        taskOrgList.addAll(receiverOrgList);
        return taskOrgList;
    }

    private TaskInfo toTaskInfo(TaskPB taskPB) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskPB.getTaskId());
        taskInfo.setDataId(taskPB.getDataId());
        taskInfo.setDataStatus(taskPB.getDataStatusValue());
        taskInfo.setUser(taskPB.getUser());
        taskInfo.setUserType(taskPB.getUserTypeValue());
        taskInfo.setTaskName(taskPB.getTaskName());
        taskInfo.setDataPolicyTypesList(taskPB.getDataPolicyTypesList());
        taskInfo.setPowerPolicyTypesList(taskPB.getPowerPolicyTypesList());
        taskInfo.setDataFlowPolicyTypesList(taskPB.getDataFlowPolicyTypesList());
        taskInfo.setReceiverPolicyTypesList(taskPB.getReceiverPolicyTypesList());
        taskInfo.setMetaAlgorithmId(taskPB.getMetaAlgorithmId());
        taskInfo.setState(taskPB.getStateValue());
        taskInfo.setReason(taskPB.getReason());
        taskInfo.setDesc(taskPB.getDesc());
        taskInfo.setCreateAt(LocalDateTimeUtil.getLocalDateTme(taskPB.getCreateAt()));
        taskInfo.setStartAt(LocalDateTimeUtil.getLocalDateTme(taskPB.getStartAt()));
        taskInfo.setEndAt(LocalDateTimeUtil.getLocalDateTme(taskPB.getEndAt()));
        taskInfo.setSign(taskPB.getSign().toString());
        taskInfo.setNonce(taskPB.getNonce());
        taskInfo.setInitMemory(taskPB.getOperationCost().getMemory());
        taskInfo.setInitProcessor(taskPB.getOperationCost().getProcessor());
        taskInfo.setInitBandwidth(taskPB.getOperationCost().getBandwidth());
        taskInfo.setInitDuration(taskPB.getOperationCost().getDuration());
        return taskInfo;
    }

    private TaskOrg toTaskOrg(String taskId, TaskRoleEnum taskRole, Base.TaskOrganization taskOrganization) {
        TaskOrg org = new TaskOrg();
        org.setTaskId(taskId);
        org.setTaskRole(taskRole.getRole());
        org.setPartyId(taskOrganization.getPartyId());
        org.setNodeName(taskOrganization.getNodeName());
        org.setNodeId(taskOrganization.getNodeId());
        org.setIdentityId(taskOrganization.getIdentityId());
        return org;
    }
}
