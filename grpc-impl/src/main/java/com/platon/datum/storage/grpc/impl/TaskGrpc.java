package com.platon.datum.storage.grpc.impl;

import carrier.types.Common;
import carrier.types.Resourcedata;
import carrier.types.Taskdata;
import com.platon.datum.storage.common.enums.CodeEnums;
import com.platon.datum.storage.common.util.LocalDateTimeUtil;
import com.platon.datum.storage.dao.entity.TaskEvent;
import com.platon.datum.storage.dao.entity.TaskInfo;
import com.platon.datum.storage.dao.entity.TaskOrg;
import com.platon.datum.storage.dao.entity.TaskOrg.TaskRoleEnum;
import com.platon.datum.storage.dao.entity.TaskPowerResourceOptions;
import com.platon.datum.storage.grpc.utils.GrpcImplUtils;
import com.platon.datum.storage.service.*;
import datacenter.api.Task;
import datacenter.api.TaskServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
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

    @Resource
    private TaskEventService taskEventService;

    @Resource
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
    private TaskPowerResourceOptionsService taskPowerResourceOptionsService;

    @Resource
    private TaskReceiverOptionService taskReceiverOptionService;


    /**
     * <pre>
     * 存储任务
     * </pre>
     */
    @Override
    public void saveTask(Task.SaveTaskRequest request,
                         io.grpc.stub.StreamObserver<Common.SimpleResponse> responseObserver) {
        Common.SimpleResponse response = GrpcImplUtils.saveOfUpdate(
                request,
                input -> saveTaskInternal(input),
                "saveTask");
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void saveTaskInternal(Task.SaveTaskRequest request) {
        Taskdata.TaskPB taskPB = request.getTask();
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
        taskPowerResourceOptionsService.savePowerResourceOption(toPowerResourceOption(taskPB));
        taskReceiverOptionService.saveReceiverOption(taskId, taskPB.getReceiverPolicyOptionsList());

        //==任务日志
        List<TaskEvent> taskEvents = toTaskEventList(taskPB);
        if (!CollectionUtils.isEmpty(taskEvents)) {
            taskEventService.insert(taskEvents);
        }
    }

    /**
     * <pre>
     * 查询任务详情（任务ID、节点ID、参与方标识）
     * </pre>
     */
    @Override
    public void getTaskDetail(Task.GetTaskDetailRequest request,
                              io.grpc.stub.StreamObserver<Task.GetTaskDetailResponse> responseObserver) {
        Task.GetTaskDetailResponse response = GrpcImplUtils.query(
                request,
                input -> getTaskDetailInternal(input),
                bizOut -> Task.GetTaskDetailResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .setTask(bizOut).build(),
                bizError -> Task.GetTaskDetailResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Task.GetTaskDetailResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"getTaskDetail"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Taskdata.TaskPB getTaskDetailInternal(Task.GetTaskDetailRequest request) {
        // 业务代码
        String taskId = request.getTaskId();
        TaskInfo taskInfo = taskInfoService.findByTaskId(taskId);
        Taskdata.TaskPB taskPB = convertorService.toTaskPB(taskInfo);
        return taskPB;
    }


    /**
     * <pre>
     * 查询任务列表
     * </pre>
     */
    @Override
    public void listTask(Task.ListTaskRequest request,
                         io.grpc.stub.StreamObserver<Task.ListTaskResponse> responseObserver) {
        Task.ListTaskResponse response = GrpcImplUtils.query(
                request,
                input -> listTaskInternal(input),
                bizOut -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllTasks(bizOut).build(),
                bizError -> Task.ListTaskResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listTask"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Taskdata.TaskPB> listTaskInternal(Task.ListTaskRequest request) {

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<TaskInfo> taskInfoList = taskInfoService.syncTaskInfo(lastUpdateAt, request.getPageSize());

        List<Taskdata.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);
        return grpcTaskList;
    }


    @Override
    public void listTaskByIdentity(Task.ListTaskByIdentityRequest request,
                                   io.grpc.stub.StreamObserver<Task.ListTaskResponse> responseObserver) {
        Task.ListTaskResponse response = GrpcImplUtils.query(
                request,
                input -> listTaskByIdentityInternal(input),
                bizOut -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllTasks(bizOut).build(),
                bizError -> Task.ListTaskResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listTaskByIdentity"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private List<Taskdata.TaskPB> listTaskByIdentityInternal(Task.ListTaskByIdentityRequest request) {

        LocalDateTime lastUpdateAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        if (request.getLastUpdated() > 0) {
            lastUpdateAt = LocalDateTimeUtil.getLocalDateTme(request.getLastUpdated());
        }

        List<TaskInfo> taskInfoList = taskInfoService.listTaskInfoByIdentityId(request.getIdentityId(), lastUpdateAt, request.getPageSize());

        List<Taskdata.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);

        return grpcTaskList;
    }

    /**
     * <pre>
     * 根据任务Ids查询任务列表 (v3.0)
     * </pre>
     */
    @Override
    public void listTaskByTaskIds(Task.ListTaskByTaskIdsRequest request,
                                  io.grpc.stub.StreamObserver<Task.ListTaskResponse> responseObserver) {

        Task.ListTaskResponse response = GrpcImplUtils.query(
                request,
                input -> listTaskByTaskIdsInternal(input),
                bizOut -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllTasks(bizOut).build(),
                bizError -> Task.ListTaskResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Task.ListTaskResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listTaskByIdentity"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Taskdata.TaskPB> listTaskByTaskIdsInternal(Task.ListTaskByTaskIdsRequest request) {

        log.debug("listTaskByTaskIds, request:{}", request);

        List<TaskInfo> taskInfoList = taskInfoService.listTaskInfoByTaskIds(request.getTaskIdsList());

        List<Taskdata.TaskPB> grpcTaskList = convertorService.toTaskPB(taskInfoList);
        return grpcTaskList;
    }

    /**
     * <pre>
     * 查询任务的事件列表
     * </pre>
     */
    @Override
    public void listTaskEvent(Task.ListTaskEventRequest request,
                              io.grpc.stub.StreamObserver<Task.ListTaskEventResponse> responseObserver) {
        Task.ListTaskEventResponse response = GrpcImplUtils.query(
                request,
                input -> listTaskEventInternal(input),
                bizOut -> Task.ListTaskEventResponse.newBuilder()
                        .setStatus(CodeEnums.SUCCESS.getCode())
                        .setMsg(CodeEnums.SUCCESS.getMessage())
                        .addAllTaskEvents(bizOut).build(),
                bizError -> Task.ListTaskEventResponse.newBuilder()
                        .setStatus(bizError.getCode())
                        .setMsg(bizError.getMessage())
                        .build(),
                error -> Task.ListTaskEventResponse.newBuilder()
                        .setStatus(CodeEnums.EXCEPTION.getCode())
                        .setMsg(error.getMessage())
                        .build(),"listTaskEvent"
        );
        // 返回
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<Taskdata.TaskEvent> listTaskEventInternal(Task.ListTaskEventRequest request) {
        List<TaskEvent> taskEventList = taskEventService.listTaskEventByTaskId(request.getTaskId());

        List<Taskdata.TaskEvent>
                grpcTaskEventList = convertorService.toProtoTaskEvent(taskEventList);

        return grpcTaskEventList;
    }


    private List<TaskEvent> toTaskEventList(Taskdata.TaskPB taskPB) {
        List<TaskEvent> taskEventList = new ArrayList<>();
        for (Taskdata.TaskEvent event : taskPB.getTaskEventsList()) {
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

    private List<TaskPowerResourceOptions> toPowerResourceOption(Taskdata.TaskPB taskPB) {
        List<TaskPowerResourceOptions> list = taskPB.getPowerResourceOptionsList().stream()
                .map(taskPowerResourceOption -> {
                    TaskPowerResourceOptions option = new TaskPowerResourceOptions();
                    option.setTaskId(taskPB.getTaskId());
                    option.setPartId(taskPowerResourceOption.getPartyId());
                    Resourcedata.ResourceUsageOverview resourceUsedOverview = taskPowerResourceOption.getResourceUsedOverview();
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

    private List<TaskOrg> toTaskOrgList(Taskdata.TaskPB taskPB) {
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

    private TaskInfo toTaskInfo(Taskdata.TaskPB taskPB) {
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

    private TaskOrg toTaskOrg(String taskId, TaskRoleEnum taskRole, Taskdata.TaskOrganization taskOrganization) {
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
