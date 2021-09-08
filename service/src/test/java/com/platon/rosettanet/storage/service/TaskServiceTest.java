package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.OrgInfo;
import com.platon.rosettanet.storage.dao.entity.Task;
import com.platon.rosettanet.storage.dao.entity.TaskMetaDataColumn;
import com.platon.rosettanet.storage.grpc.lib.common.CommonStatus;
import com.platon.rosettanet.storage.grpc.lib.common.TaskState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@Transactional //这个有看需要，测试方法如果要作为一个整体事务，则加上
@Rollback(true) // 默认值：true, UT默认都会回滚数据库，不会增加新数据
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskMetaDataColumnService taskMetaDataColumnService;
    @Autowired
    private OrgInfoService orgInfoService;

    @Test
    public void insert() {
        Task task = new Task();
        task.setId("testTask");
        task.setTaskName("testTask");
        task.setOwnerIdentityId("ownerId");
        task.setRequiredMemory(128L);
        task.setRequiredCore(3);
        task.setRequiredBandwidth(100L);
        task.setUsedMemory(128L);
        task.setUsedCore(3);
        task.setUsedBandwidth(100L);
        task.setRequiredDuration(600000L);
        task.setCreateAt(LocalDateTime.now());
        task.setStartAt(LocalDateTime.now());
        task.setEndAt(LocalDateTime.now().plusDays(2));
        task.setStatus(TaskState.TaskState_Succeed.ordinal());
        taskService.insert(task);


        List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
        for(int i=0; i<100; i++){
            TaskMetaDataColumn taskMetaDataColumn = new TaskMetaDataColumn();
            taskMetaDataColumn.setTaskId("testTask");
            taskMetaDataColumn.setMetaDataId("metaDataId");
            taskMetaDataColumn.setColumnIdx(i);
            taskMetaDataColumnList.add(taskMetaDataColumn);
        }
        taskMetaDataColumnService.insert(taskMetaDataColumnList);


        OrgInfo orgInfo = new OrgInfo();
        orgInfo.setIdentityId("identityId");
        orgInfo.setIdentityType("DID");
        orgInfo.setOrgName("orgName");
        orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());

        orgInfoService.insert(orgInfo);
    }
}
