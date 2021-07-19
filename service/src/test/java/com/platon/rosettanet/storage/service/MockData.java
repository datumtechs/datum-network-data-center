package com.platon.rosettanet.storage.service;

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
    private PowerServerService powerServerService;
    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private TaskMetaDataService taskMetaDataService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    @Autowired
    private TaskResultConsumerService taskResultConsumerService;

    @Autowired
    private TaskEventService taskEventService;

    static int orgCount = 100;
    static int eachOrgDataFileCount = 100;
    static int eachDataFileColumns = 10;
    static int eachOrgPowerServerCount = 10;
    static int taskCount = 100;
    static int eachTaskDataProviderCount = 10;
    static int eachTaskDataProviderColumnCount = 10; //<=eachDataFileColumns
    static int eachTaskPowerProviderCount = 10;
    static int eachTaskResultConsumerCount = 5;
    static int eachTaskResultConsumerSenderCount = 2;


    @Test
    public void initMockData() {



        List<OrgInfo> orgInfoList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            OrgInfo orgInfo = new OrgInfo();
            orgInfo.setIdentityId("identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ));
            orgInfo.setIdentityType("DID");
            orgInfo.setOrgName("orgName_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ) );
            orgInfo.setStatus("enabled");
            orgInfo.setNodeId("nodeId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ));
            orgInfoList.add(orgInfo);

        }
        orgInfoService.insert(orgInfoList);


        /*List<DataServer> dataServerList = new ArrayList<>();
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
        dataServerMapper.insertBatch(dataServerList);*/

        List<PowerServer> powerServerList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            for(int j=1; j<eachOrgPowerServerCount; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );

                PowerServer powerServer = new PowerServer();
                powerServer.setIdentityId(identityId);
                powerServer.setId("powerId_" + extID);
                powerServer.setCore(j);
                powerServer.setMemory((long) j);
                powerServer.setBandwidth((long) j);
                powerServer.setPublished(true);
                powerServer.setPublishedAt(randomDay());
                powerServerList.add(powerServer);
            }
        }
        powerServerService.insert(powerServerList);


        List<DataFile> dataFileList = new ArrayList<>();
        List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            for(int j=1; j<eachOrgDataFileCount; j++){
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
                dataFile.setStatus("release");
                dataFile.setRemarks("dataFileRemarks_" + extID);
                dataFileList.add(dataFile);

                //List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
                for(int k=1; k<=eachDataFileColumns; k++){
                    MetaDataColumn column = new MetaDataColumn();
                    column.setMetaDataId(metaDataId);
                    column.setColumnIdx(k);
                    column.setColumnType("String");
                    column.setColumnName("columnName_" + extID + "_" + k);
                    column.setColumnSize(RandomUtils.nextInt(1, 10000));
                    column.setRemarks("columnRemarks_" + extID + "_" + k);
                    column.setPublished(true);
                    metaDataColumnList.add(column);
                }
                //metaDataService.insertMetaData(dataFile, metaDataColumnList);
            }
        }
        metaDataService.insertDataFile(dataFileList);
        metaDataService.insertMetaDataColumn(metaDataColumnList);


        List<Task> taskList = new ArrayList<>();
        List<TaskAlgoProvider> taskAlgoProviderList = new ArrayList<>();
        List<TaskMetaData> taskMetaDataList = new ArrayList<>();
        List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
        List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
        List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();
        List<TaskEvent> taskEventList = new ArrayList<>();

        for(int i=1; i<=taskCount; i++) {
            String taskId = StringUtils.leftPad(String.valueOf(i) , 6, "0" );
            String ownerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );
            String ownerPartyId = "partyId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, 101)) , 6, "0" );

            Task task = new Task();
            task.setId("taskId_" + taskId);
            task.setTaskName("taskName_" + taskId);
            task.setOwnerIdentityId(ownerIdentityId);
            task.setOwnerPartyId(ownerPartyId);

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

            //算法提供者（和owner一样）
            TaskAlgoProvider taskAlgoProvider = new TaskAlgoProvider();
            taskAlgoProvider.setTaskId(taskId);
            taskAlgoProvider.setIdentityId(ownerIdentityId);
            taskAlgoProvider.setPartyId(ownerPartyId);
            taskAlgoProviderList.add(taskAlgoProvider);


            Map<String, Boolean> partnerIdMap = new HashMap<>();

            for (int j=1; j<=eachTaskDataProviderCount; j++) {

                //随机挑选1个不同的org来提供data file
                String dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                String dataPartyId = StringUtils.replace(dataIdentityId,"identityId", "partyId");

                String metaDataId = StringUtils.replace(dataIdentityId,"identityId", "metaDataId") + "_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, eachOrgDataFileCount+1)), 6, "0");

                while (partnerIdMap.containsKey(dataIdentityId)) {
                    dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                    dataPartyId = StringUtils.replace(dataIdentityId,"identityId", "partyId");
                    metaDataId = StringUtils.replace(dataIdentityId,"identityId", "metaDataId") + "_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, eachOrgDataFileCount+1)), 6, "0");
                }
                partnerIdMap.put(dataIdentityId, Boolean.TRUE);

                //随机此org的一个dataFile

                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(task.getId());
                taskMetaData.setPartyId(dataPartyId);
                taskMetaData.setMetaDataId(metaDataId);

                taskMetaDataList.add(taskMetaData);
                //taskMetaDataService.insert(taskMetaData);

                //List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
                for (int k = 1; k <= eachTaskDataProviderColumnCount; k++) {
                    TaskMetaDataColumn taskMetaDataColumn = new TaskMetaDataColumn();
                    taskMetaDataColumn.setTaskId(task.getId());
                    taskMetaDataColumn.setMetaDataId(metaDataId);
                    taskMetaDataColumn.setColumnIdx(k);
                    taskMetaDataColumnList.add(taskMetaDataColumn);
                }
                //taskMetaDataColumnService.insert(taskMetaDataColumnList);
            }

            //List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (int jj=1; jj<=eachTaskPowerProviderCount; jj++) {//每个任务10个不同的powerProvider
                //随机挑选1个不同的org来提供power
                String powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                String powerPartyId = StringUtils.replace(powerIdentityId,"identityId", "partyId");
                while (partnerIdMap.containsKey(powerIdentityId)) {
                    powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                    powerPartyId = StringUtils.replace(powerIdentityId,"identityId", "partyId");
                }
                partnerIdMap.put(powerIdentityId, Boolean.TRUE);

                TaskPowerProvider taskPowerProvider = new TaskPowerProvider();
                taskPowerProvider.setTaskId(task.getId());
                taskPowerProvider.setIdentityId(powerIdentityId);
                taskPowerProvider.setPartyId(powerPartyId);

                taskPowerProviderList.add(taskPowerProvider);
            }
            //taskPowerProviderService.insert(taskPowerProviderList);


            //partnerIdMap中，随机2个resultReceiver, 每个Receiver2个sender
            //List<TaskResultConsumer> taskResultConsumerList = new ArrayList<>();

            List<String> partnerIdList = new ArrayList<String>(partnerIdMap.keySet());
            int partnerIdCount = partnerIdList.size();
            Map<String, Boolean> usedIdMap = new HashMap<>();
            for (int m=0; m<eachTaskResultConsumerCount; m++) {
                //从任务参与方中选择一个不同的结果接受者
                String receiverId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                while (usedIdMap.containsKey(receiverId)) {
                    receiverId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                }
                usedIdMap.put(receiverId, Boolean.TRUE);

                for (int n=0; n<eachTaskResultConsumerSenderCount; n++) {
                    //从任务参与方中选择一个不同的结果接受者
                    String senderId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                    while (usedIdMap.containsKey(senderId)) {
                        senderId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));
                    }
                    usedIdMap.put(senderId, Boolean.TRUE);

                    TaskResultConsumer resultConsumer = new TaskResultConsumer();
                    resultConsumer.setTaskId(task.getId());
                    resultConsumer.setConsumerIdentityId(receiverId);
                    resultConsumer.setConsumerPartyId(StringUtils.replace(receiverId,"identityId", "partyId"));
                    resultConsumer.setProducerIdentityId(senderId);
                    resultConsumer.setProducerPartyId(StringUtils.replace(senderId,"identityId", "partyId"));
                    taskResultConsumerList.add(resultConsumer);
                }
            }
            //taskResultConsumerService.insert(taskResultConsumerList);

            //task event
            //每个任务20个事件
            for (int j=0; j<20; j++){
                String identityId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdCount));

                TaskEvent event = new TaskEvent();
                event.setTaskId(taskId);
                event.setIdentityId(identityId);
                event.setEventAt(LocalDateTime.now());
                event.setEventType("eventType");
                event.setEventContent("eventContent_" + taskId + "_" + identityId);
                taskEventList.add(event);
            }
        }



        taskService.insert(taskList);
        taskMetaDataService.insert(taskMetaDataList);
        taskMetaDataColumnService.insert(taskMetaDataColumnList);
        taskPowerProviderService.insert(taskPowerProviderList);
        taskResultConsumerService.insert(taskResultConsumerList);
        taskEventService.insert(taskEventList);
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
