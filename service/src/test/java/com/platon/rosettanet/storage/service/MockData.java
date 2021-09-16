package com.platon.rosettanet.storage.service;

import com.platon.rosettanet.storage.dao.entity.*;
import com.platon.rosettanet.storage.grpc.lib.common.CommonStatus;
import com.platon.rosettanet.storage.grpc.lib.common.DataStatus;
import com.platon.rosettanet.storage.grpc.lib.common.MetadataState;
import com.platon.rosettanet.storage.grpc.lib.common.TaskState;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
    private TaskAlgoProviderService taskAlgoProviderService;

    @Autowired
    private TaskMetaDataService taskMetaDataService;

    @Autowired
    private TaskPowerProviderService taskPowerProviderService;

    @Autowired
    private TaskResultConsumerService taskResultConsumerService;

    @Autowired
    private TaskEventService taskEventService;

    @Autowired
    private MetaDataAuthService metaDataAuthService;

    static int orgCount = 30;
    static int eachOrgDataFileCount = 10;
    static int eachDataFileColumns = 5;
    static int eachOrgPowerServerCount = 10;
    static int taskCount = 20;

    // eachTaskDataProviderCount + eachTaskPowerProviderCount >= eachTaskResultConsumerCount + eachTaskResultConsumerCount * eachTaskResultConsumerSenderCount
    static int eachTaskDataProviderCount = 5;
    static int eachTaskDataProviderColumnCount = 5; //<=eachDataFileColumns
    static int eachTaskPowerProviderCount = 5;
    static int eachTaskResultConsumerCount = 3;
    //static int eachTaskResultConsumerSenderCount = 4;   //eachTaskDataProviderCount + eachTaskPowerProviderCount >= eachTaskResultConsumerCount*（ eachTaskResultConsumerCount + eachTaskResultConsumerSenderCount）


    @Test
    public void initMockDataFile() {
        List<OrgInfo> orgInfoList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            System.out.println(identityId);

            OrgInfo orgInfo = new OrgInfo();
            orgInfo.setIdentityId(identityId);
            orgInfo.setIdentityType("DID");
            orgInfo.setOrgName("orgName_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ) );
            orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());
            orgInfo.setNodeId("nodeId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ));
            orgInfoList.add(orgInfo);

        }
        orgInfoService.insert(orgInfoList);


        List<DataFile> dataFileList = new ArrayList<>();
        List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            for(int j=1; j<=eachOrgDataFileCount; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );
                String metaDataId = "metaData:0x" + HexUtils.toHexString(RandomUtils.nextBytes(32));
                DataFile dataFile = new DataFile();
                dataFile.setOriginId("dataFileId_" + extID);
                dataFile.setIdentityId(identityId);
                dataFile.setMetaDataId(metaDataId);
                dataFile.setFileName("fileName_" + extID);
                dataFile.setFileType("csv");
                dataFile.setFilePath("/opt/usr/data");
                dataFile.setResourceName("resourceName_" + extID);
                dataFile.setIndustry("金融行业");
                dataFile.setSize(209715200L);
                dataFile.setRows(100000000L);
                dataFile.setColumns(eachDataFileColumns);
                dataFile.setHasTitle(true);
                dataFile.setPublished(true);
                dataFile.setPublishedAt(randomDay());
                dataFile.setStatus(DataStatus.DataStatus_Normal.ordinal());
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

    }


    @Test
    public void initMockOrgPower() {

        List<OrgInfo> orgInfoList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            System.out.println(identityId);

            OrgInfo orgInfo = new OrgInfo();
            orgInfo.setIdentityId(identityId);
            orgInfo.setIdentityType("DID");
            orgInfo.setOrgName("orgName_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ) );
            orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());
            orgInfo.setNodeId("nodeId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ));
            orgInfoList.add(orgInfo);

        }
        orgInfoService.insert(orgInfoList);


        List<PowerServer> powerServerList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            for(int j=1; j<eachOrgPowerServerCount; j++){
                String extID = identityId + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );

                PowerServer powerServer = new PowerServer();
                powerServer.setIdentityId(identityId);
                powerServer.setId("powerId_" + extID);
                powerServer.setCore(j);
                powerServer.setMemory((long) j * 1000000000);
                powerServer.setBandwidth((long) j * 1000000);
                powerServer.setPublished(true);
                powerServer.setPublishedAt(randomDay());
                powerServerList.add(powerServer);
            }
        }
        powerServerService.insert(powerServerList);
    }
    @Test
    public void initMockData() {
        List<OrgInfo> orgInfoList = new ArrayList<>();
        for(int i=1; i<=orgCount; i++){
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            OrgInfo orgInfo = new OrgInfo();
            orgInfo.setIdentityId(identityId);
            orgInfo.setIdentityType("DID");
            orgInfo.setOrgName("orgName_" + StringUtils.leftPad(String.valueOf(i), 6, "0" ) );
            orgInfo.setStatus(CommonStatus.CommonStatus_Normal.ordinal());
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
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            for(int j=1; j<eachOrgPowerServerCount; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );
                PowerServer powerServer = new PowerServer();
                powerServer.setIdentityId(identityId);
                powerServer.setId("powerId_" + extID);
                powerServer.setCore(j);
                powerServer.setMemory((long) j * 1000000000);
                powerServer.setBandwidth((long) j * 1000000);
                powerServer.setPublished(true);
                powerServer.setPublishedAt(randomDay());
                powerServerList.add(powerServer);
            }
        }
        powerServerService.insert(powerServerList);


        List<DataFile> dataFileList = new ArrayList<>();
        List<MetaDataColumn> metaDataColumnList = new ArrayList<>();
        Map<String, List<String>> orgMetaDataListMap = new HashMap<>();
        for(int i=1; i<=orgCount; i++){
            //String identityId = "identityId_" + StringUtils.leftPad(String.valueOf(i), 6, "0" );
            String identityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(i), 6, "0" ).getBytes(StandardCharsets.UTF_8));
            List<String> orgMetaDataList = new ArrayList<>();
            for(int j=1; j<=eachOrgDataFileCount; j++){
                String extID = StringUtils.leftPad(String.valueOf(i) , 6, "0" ) + "_" + StringUtils.leftPad(String.valueOf(j), 6, "0" );
                String metaDataId = "metaData:0x" + HexUtils.toHexString(RandomUtils.nextBytes(32));
                orgMetaDataList.add(metaDataId);

                DataFile dataFile = new DataFile();
                dataFile.setOriginId("dataFileId_" + extID);
                dataFile.setIdentityId(identityId);
                dataFile.setMetaDataId(metaDataId);
                dataFile.setFileName("fileName_" + extID);
                dataFile.setFileType("csv");
                dataFile.setFilePath("/opt/usr/data");
                dataFile.setResourceName("resourceName_" + extID);
                dataFile.setIndustry("金融行业");
                dataFile.setSize(1024*1024 * RandomUtils.nextLong(10, 10000));
                dataFile.setRows(RandomUtils.nextLong(10000, 1000000));
                dataFile.setColumns(eachDataFileColumns);
                dataFile.setHasTitle(true);
                dataFile.setPublished(true);
                dataFile.setPublishedAt(randomDay());
                dataFile.setStatus(MetadataState.MetadataState_Released.ordinal());
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
            orgMetaDataListMap.put(identityId, orgMetaDataList);
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
        List<MetaDataAuth> metaDataAuthList = new ArrayList<>();

        for(int i=1; i<=taskCount; i++) {
            String taskId = StringUtils.leftPad(String.valueOf(i) , 6, "0" );
            //String ownerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount)) , 6, "0" );
            //String ownerPartyId = "partyId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount)) , 6, "0" );
            String ownerIdentityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount)) , 6, "0" ).getBytes(StandardCharsets.UTF_8));
            String ownerPartyId = StringUtils.replace(ownerIdentityId,"identity_", "partyId_");
            Task task = new Task();
            task.setId("taskId_" + taskId);
            task.setTaskName("taskName_" + taskId);
            task.setOwnerIdentityId(ownerIdentityId);
            task.setOwnerPartyId(ownerPartyId);
            task.setUserType(1); // 1: 以太坊地址; 2: Alaya地址; 3: PlatON地址'
            task.setUserId(UUID.randomUUID().toString());

            LocalDateTime createAt = randomDay();

            task.setCreateAt(createAt);
            task.setStartAt(createAt.plusDays(1));
            task.setEndAt(createAt.plusDays(2));
            task.setRequiredCore(10);
            task.setRequiredMemory(1000000000L);
            task.setRequiredBandwidth(1000000L);
            task.setRequiredDuration(1000000L);
            task.setUsedCore(10);
            task.setUsedMemory(1000000L);
            task.setUsedBandwidth(1000000L);
            task.setStatus(TaskState.TaskState_Succeed.ordinal());

            taskList.add(task);
            //taskService.insert(task);

            //算法提供者（和owner一样）
            TaskAlgoProvider taskAlgoProvider = new TaskAlgoProvider();
            taskAlgoProvider.setTaskId("taskId_" + taskId);
            taskAlgoProvider.setIdentityId(ownerIdentityId);
            taskAlgoProvider.setPartyId(ownerPartyId);
            taskAlgoProviderList.add(taskAlgoProvider);


            Map<String, Boolean> partnerIdMap = new HashMap<>();
            partnerIdMap.put(ownerIdentityId, true);//任务发起者，不再提供算力和数据，但是可以接收任务结果


            for (int j=1; j<=eachTaskDataProviderCount; j++) {

                //随机挑选1个不同的org来提供data file
                //String dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                String dataIdentityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" ).getBytes(StandardCharsets.UTF_8));
                String dataPartyId = StringUtils.replace(dataIdentityId,"identity_", "partyId_");


                //String metaDataId = StringUtils.replace(dataIdentityId,"identityId", "metaDataId") + "_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, eachOrgDataFileCount+1)), 6, "0");
                String metaDataId = orgMetaDataListMap.get(dataIdentityId).get(RandomUtils.nextInt(0, eachOrgDataFileCount));

                while (partnerIdMap.containsKey(dataIdentityId)) {
                    //重新生成
                    //dataIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                    dataIdentityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" ).getBytes(StandardCharsets.UTF_8));

                    dataPartyId = StringUtils.replace(dataIdentityId,"identity_", "partyId_");
                    //metaDataId = StringUtils.replace(dataIdentityId,"identityId", "metaDataId") + "_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, eachOrgDataFileCount+1)), 6, "0");
                    metaDataId = orgMetaDataListMap.get(dataIdentityId).get(RandomUtils.nextInt(0, eachOrgDataFileCount));
                }
                partnerIdMap.put(dataIdentityId, Boolean.TRUE);

                //随机此org的一个dataFile
                TaskMetaData taskMetaData = new TaskMetaData();
                taskMetaData.setTaskId(task.getId());
                //冗余
                taskMetaData.setIdentityId(dataIdentityId);
                taskMetaData.setPartyId(dataPartyId);
                taskMetaData.setMetaDataId(metaDataId);

                taskMetaDataList.add(taskMetaData);
                //taskMetaDataService.insert(taskMetaData);

                //List<TaskMetaDataColumn> taskMetaDataColumnList = new ArrayList<>();
                taskMetaData.setKeyColumnIdx(1); //第1列是主键列，其它列是参与计算列
                for (int k = 2; k <= eachTaskDataProviderColumnCount; k++) {
                    TaskMetaDataColumn taskMetaDataColumn = new TaskMetaDataColumn();
                    taskMetaDataColumn.setTaskId(task.getId());
                    taskMetaDataColumn.setMetaDataId(metaDataId);
                    taskMetaDataColumn.setSelectedColumnIdx(k);
                    taskMetaDataColumnList.add(taskMetaDataColumn);
                }
                //taskMetaDataColumnService.insert(taskMetaDataColumnList);


                //每个任务的metaData，都授权给任务的参与者，包括发起者，其它数据提供者，算力提供者
                MetaDataAuth metaDataAuth = new MetaDataAuth();
                metaDataAuth.setMetaDataId(taskMetaData.getMetaDataId());
                metaDataAuth.setMetaDataAuthId(UUID.randomUUID().toString());
                metaDataAuth.setStatus(1);//审核通过
                metaDataAuth.setApplyAt(LocalDateTime.now(ZoneOffset.UTC));
                metaDataAuth.setUserIdentityId(task.getOwnerIdentityId());
                metaDataAuth.setUserId(task.getUserId());
                metaDataAuth.setUserType(1);
                metaDataAuth.setAuthType(2);  //2: 按照次数来使用
                metaDataAuth.setTimes(100);
                metaDataAuthList.add(metaDataAuth);
            }

            //List<TaskPowerProvider> taskPowerProviderList = new ArrayList<>();
            for (int jj=1; jj<=eachTaskPowerProviderCount; jj++) {//每个任务10个不同的powerProvider
                //随机挑选1个不同的org来提供power
                //String powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                String powerIdentityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" ).getBytes(StandardCharsets.UTF_8));
                String powerPartyId = StringUtils.replace(powerIdentityId,"identity_", "partyId_");

                while (partnerIdMap.containsKey(powerIdentityId)) {
                    //重新生成
                    //powerIdentityId = "identityId_" + StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" );
                    powerIdentityId = "identity_" + DigestUtils.md5DigestAsHex(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(1, orgCount+1)) , 6, "0" ).getBytes(StandardCharsets.UTF_8));
                    powerPartyId = StringUtils.replace(powerIdentityId,"identity_", "partyId_");
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
            List<String> partnerIdListCopy = new ArrayList<String>();
            partnerIdListCopy.addAll(partnerIdList);

            int partnerIdCount = partnerIdList.size();
            //System.out.println("hahahha：partnerIdCount:" + partnerIdCount + "    xxx:" + eachTaskResultConsumerCount*eachTaskResultConsumerSenderCount );
            for (int m=0; m<eachTaskResultConsumerCount; m++) {
                //从任务参与方中选择一个不同的结果接受者
                String receiverId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdList.size()));
                partnerIdList.remove(receiverId);
                TaskResultConsumer resultConsumer = new TaskResultConsumer();
                resultConsumer.setTaskId(task.getId());
                resultConsumer.setConsumerIdentityId(receiverId);
                resultConsumer.setConsumerPartyId(StringUtils.replace(receiverId,"identityId", "partyId"));
                    /*resultConsumer.setProducerIdentityId(senderId);
                    resultConsumer.setProducerPartyId(StringUtils.replace(senderId,"identityId", "partyId"));*/
                taskResultConsumerList.add(resultConsumer);
                /*for (int n=0; n<eachTaskResultConsumerSenderCount; n++) {
                    //从任务参与方中选择一个不同的结果接受者
                    //String senderId = partnerIdList.get(RandomUtils.nextInt(0, partnerIdList.size()));
                    //partnerIdList.remove(senderId);

                    TaskResultConsumer resultConsumer = new TaskResultConsumer();
                    resultConsumer.setTaskId(task.getId());
                    resultConsumer.setConsumerIdentityId(receiverId);
                    resultConsumer.setConsumerPartyId(StringUtils.replace(receiverId,"identityId", "partyId"));
                    *//*resultConsumer.setProducerIdentityId(senderId);
                    resultConsumer.setProducerPartyId(StringUtils.replace(senderId,"identityId", "partyId"));*//*
                    taskResultConsumerList.add(resultConsumer);
                }*/
            }
            //taskResultConsumerService.insert(taskResultConsumerList);

            //task event
            //每个任务20个事件
            for (int j=0; j<2; j++){
                String identityId = partnerIdListCopy.get(RandomUtils.nextInt(0, partnerIdCount));

                TaskEvent event = new TaskEvent();
                event.setTaskId("taskId_" + taskId);
                event.setIdentityId(identityId);
                event.setEventAt(LocalDateTime.now(ZoneOffset.UTC));
                event.setEventType("eventType");
                event.setEventContent("eventContent_" + taskId + "_" + identityId);
                taskEventList.add(event);
            }
        }



        taskService.insert(taskList);
        taskAlgoProviderService.insertBatch(taskAlgoProviderList);
        taskMetaDataService.insert(taskMetaDataList);
        taskMetaDataColumnService.insert(taskMetaDataColumnList);
        taskPowerProviderService.insert(taskPowerProviderList);
        taskResultConsumerService.insert(taskResultConsumerList);
        taskEventService.insert(taskEventList);
        metaDataAuthService.insert(metaDataAuthList);
    }

    private LocalDateTime randomDay(){
        int gaps = 60;
        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC).minusDays(gaps);
        Duration duration = Duration.between(start, LocalDateTime.now(ZoneOffset.UTC));
        long days = duration.toDays(); //相差的天数
        int random = RandomUtils.nextInt(0, (int)days);
        return start.plusDays(random);
    }
}
