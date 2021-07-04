package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.DataServerMapper;
import com.platon.rosettanet.storage.dao.entity.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional //这个有看需要，测试方法如果要作为一个整体事务，则加上
@Rollback(false) // 默认值：true, UT默认都会回滚数据库，不会增加新数据
public class MockData {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskMetaDataColumnService taskMetaDataColumnService;
    @Autowired
    private OrgInfoService orgInfoService;
    @Autowired
    private DataServerMapper dataServerMapper;
    @Autowired
    private PowerServerService powerServerService;
    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private TaskMetaDataService taskMetaDataService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    @Autowired
    private TaskResultConsumerService taskResultConsumerService;

    @Test
    public void initMockData() {

        List<OrgInfo> orgInfoList = new ArrayList<>();
        for(int i=1; i<=100; i++){
            OrgInfo orgInfo = new OrgInfo();
            orgInfo.setIdentityId("identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ));
            orgInfo.setIdentityType("DID");
            orgInfo.setOrgName("orgName_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ) );
            orgInfo.setStatus("enabled");
            orgInfoList.add(orgInfo);

        }
        orgInfoService.insert(orgInfoList);


        List<DataServer> dataServerList = new ArrayList<>();
        for(int i=1; i<=100; i++){
            String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            for(int j=1; j<20; j++){
                DataServer dataServer = new DataServer();
                dataServer.setIdentityId(identityId);
                dataServer.setId("dataServerId_" + StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" ));
                dataServer.setServerName("dataServerName_" + StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" ));
                dataServer.setPublished(true);
                dataServer.setStatus("enabled");
                dataServer.setPublishedAt(randomDay());
                dataServerList.add(dataServer);
            }
        }
        dataServerMapper.insertBatch(dataServerList);

        List<PowerServer> powerServerList = new ArrayList<>();
        for(int i=1; i<=100; i++){
            String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            for(int j=1; j<20; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );

                PowerServer powerServer = new PowerServer();
                powerServer.setIdentityId(identityId);
                powerServer.setId("dataServerId_" + extID);
                powerServer.setServerName("dataServerName_" + extID);
                powerServer.setCore(j);
                powerServer.setMemory((long) j);
                powerServer.setBandwidth((long) j);
                powerServer.setPublished(true);
                powerServer.setStatus("enabled");
                powerServer.setPublishedAt(randomDay());
                powerServerList.add(powerServer);
            }
        }
        powerServerService.insert(powerServerList);


        List<DataFile> dataFileList = new ArrayList<>();
        List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
        for(int i=1; i<=100; i++){
            String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            for(int j=1; j<20; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );

                String metaDataId = "metaDataId_" + extID;
                DataFile dataFile = new DataFile();
                dataFile.setId("dataFileId_" + extID);
                dataFile.setIdentityId(identityId);
                dataFile.setMetaDataId(metaDataId);
                dataFile.setFileName("fileName_" + extID);
                dataFile.setFileType("csv");
                dataFile.setFilePath("/opt/usr/data");
                dataFile.setResourceName("resourceName_" + extID);
                dataFile.setSize(100000000000000L);
                dataFile.setRows(100000000L);
                dataFile.setColumns(100);
                dataFile.setHasTitle(true);
                dataFile.setPublished(true);
                dataFile.setPublishedAt(randomDay());
                dataFile.setStatus("enabled");
                dataFileList.add(dataFile);

                //List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
                for(int k=1; k<=10; k++){
                    MetaDataColumn column = new MetaDataColumn();
                    column.setMetaDataId(metaDataId);
                    column.setColumnIdx(k);
                    column.setColumnType("String");
                    column.setColumnName("columnName_" + extID + "_" + k);
                    column.setPublished(true);
                    metaDataColumnList.add(column);
                }
                //metaDataService.insertMetaData(dataFile, metaDataColumnList);
            }
        }
        metaDataService.insertDataFile(dataFileList);
        metaDataService.insertMetaDataColumn(metaDataColumnList);


        List<Task> taskList = new ArrayList<>();
        List<TaskMetaData> taskMetaDataList = new ArrayList<>();
        List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
        List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
        List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
        //任务数
        int taskCount = 200;
        for(int i=1; i<=taskCount; i++) {
            String taskId = StringUtils.leftPad(String.valueOf(i) , 6, "0" );
            String ownerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );

            Task task = new Task();
            task.setId("taskId_" + taskId);
            task.setTaskName("taskName_" + taskId);
            task.setOwnerIdentityId(ownerIdentityId);
            LocalDateTime createAt = randomDay();

            task.setCreateAt(createAt);
            task.setStartAt(createAt.plusDays(1));
            task.setEndAt(createAt.plusDays(2));
            task.setRequiredCore(10);
            task.setRequiredMemory(10000L);
            task.setRequiredBandwidth(1000L);
            task.setRequiredDuration(100000L);
            task.setUsedCore(10);
            task.setUsedMemory(10000L);
            task.setUsedBandwidth(1000L);
            task.setStatus("success");

            taskList.add(task);
            //taskService.insert(task);

            Map<String, Boolean> partnerIdMap = new HashMap<>();

            for (int j=1; j<=10; j++) {//每个任务10个不同的metaDataProvider

                String dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );
                while (partnerIdMap.containsKey(dataIdentityId)) {
                    dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );
                }
                partnerIdMap.put(dataIdentityId, Boolean.TRUE);

                //随机此identity_id下的一个dataFile
                String metaDataId = "metaDataId_" + dataIdentityId + "_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 21)), 6, "0");

                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(task.getId());
                taskMetaData.setMetaDataId(metaDataId);

                taskMetaDataList.add(taskMetaData);
                //taskMetaDataService.insert(taskMetaData);

                //List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
                for (int k = 1; k <= 10; k++) {
                    TaskMetaDataColumn taskMetaDataColumn = new TaskMetaDataColumn();
                    taskMetaDataColumn.setTaskId(task.getId());
                    taskMetaDataColumn.setMetaDataId(metaDataId);
                    taskMetaDataColumn.setColumnIdx(k);
                    taskMetaDataColumnList.add(taskMetaDataColumn);
                }
                //taskMetaDataColumnService.insert(taskMetaDataColumnList);
            }

            //List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (int jj=1; jj<=10; jj++) {//每个任务10个不同的powerProvider
                String powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );
                while (partnerIdMap.containsKey(powerIdentityId)) {
                    powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );
                }
                partnerIdMap.put(powerIdentityId, Boolean.TRUE);

                TaskPowerProvider taskPowerProvider = new TaskPowerProvider();
                taskPowerProvider.setTaskId(task.getId());
                taskPowerProvider.setIdentityId(powerIdentityId);

                taskPowerProviderList.add(taskPowerProvider);
            }
            //taskPowerProviderService.insert(taskPowerProviderList);


            //partnerIdMap中，随机2个resultReceiver, 每个Receiver2个sender
            //List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();

            List<String> partnerIdList = new ArrayList<String>(partnerIdMap.keySet());
            int partnerIdCount = partnerIdList.size();
            Map<String, Boolean> usedIdMap = new HashMap<>();
            for (int m=0; m<2; m++) {
                String receiverId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                while (usedIdMap.containsKey(receiverId)) {
                    receiverId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                }
                usedIdMap.put(receiverId, Boolean.TRUE);

                for (int n=0; n<2; n++) {
                    String senderId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                    while (usedIdMap.containsKey(senderId)) {
                        senderId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                    }
                    usedIdMap.put(senderId, Boolean.TRUE);

                    TaskResultConsumer resultConsumer = new TaskResultConsumer();
                    resultConsumer.setTaskId(task.getId());
                    resultConsumer.setConsumerIdentityId(receiverId);
                    resultConsumer.setProducerIdentityId(senderId);
                    taskResultConsumerList.add(resultConsumer);
                }
            }
            //taskResultConsumerService.insert(taskResultConsumerList);
        }
        taskService.insert(taskList);
        taskMetaDataService.insert(taskMetaDataList);
        taskMetaDataColumnService.insert(taskMetaDataColumnList);
        taskPowerProviderService.insert(taskPowerProviderList);
        taskResultConsumerService.insert(taskResultConsumerList);
    }

    private LocalDateTime randomDay(){
        int gaps = 60;
        LocalDateTime start = LocalDateTime.now().minusDays(gaps);
        Duration duration = Duration.between(start, LocalDateTime.now());
        long days = duration.toDays(); //相差的天数
        int random = RandomUtils.nextInt(0, (int)days);
        return start.plusDays(random);
    }
}
