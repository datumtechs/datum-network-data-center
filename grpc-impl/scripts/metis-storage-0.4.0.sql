drop database if exists `metis_storage`;

CREATE DATABASE `metis_storage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `metis_storage`;

/*
Navicat MySQL Data Transfer

Source Server         : 192.168.9.192
Source Server Version : 50736
Source Host           : 192.168.9.192:3306
Source Database       : metis_storage

Target Server Type    : MYSQL
Target Server Version : 50736
File Encoding         : 65001

Date: 2022-04-12 10:36:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for meta_data
-- ----------------------------
DROP TABLE IF EXISTS `meta_data`;
CREATE TABLE `meta_data` (
  `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '元数据ID,hash',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '组织身份ID',
  `data_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预留',
  `data_status` int(11) NOT NULL DEFAULT '1' COMMENT '元数据在分布式存储环境中的状态 (0: DataStatus_Unknown ; DataStatus_Normal = 1; DataStatus_Deleted = 2)',
  `meta_data_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元数据的名称 (对外的表名)',
  `meta_data_type` tinyint(2) NOT NULL DEFAULT '1' COMMENT '表示该元数据是 `普通数据` 还是 `模型数据` 的元数据 (0: 未定义; 1: 普通数据元数据; 2: 模型数据元数据)',
  `data_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始数据的Hash (如果是 远端数据源, 则为 资源Id 的Hash, 如: url的Hash)',
  `desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元数据的摘要(描述)',
  `location_type` tinyint(2) NOT NULL DEFAULT '1' COMMENT '源数据的存储位置类型 (组织本地服务器、远端服务器、云等)：0-未知，1-存储在组织本地服务器上，2-存储在远端服务器上',
  `data_type` tinyint(2) NOT NULL DEFAULT '1' COMMENT '源数据的类型 (目前只有 csv)：0-未知，1-CSV',
  `industry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元数据所属的行业 (有用户自定义, 仅做展示用)',
  `status` int(11) NOT NULL DEFAULT '2' COMMENT '元数据的状态 (0: 未知; 1: 还未发布的新表; 2: 已发布的表; 3: 已撤销的表)',
  `publish_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间，精确到毫秒',
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '(状态)修改时间',
  `nonce` int(11) DEFAULT '0' COMMENT '元数据的 nonce (用来标识该元数据在所属组织中的元数据的序号, 从 0 开始递增)',
  `allow_expose` tinyint(1) DEFAULT '0' COMMENT '是否可以被曝光 (1: 可以; 0: 不可以; 如 数据原始内容可以被下载或者支持外域查看时则为1, 默认为0)',
  `token_address` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元数据对应的 dataToken 合约的地址',
  PRIMARY KEY (`meta_data_id`),
  KEY `update_at` (`update_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据信息';

-- ----------------------------
-- Table structure for meta_data_auth
-- ----------------------------
DROP TABLE IF EXISTS `meta_data_auth`;
CREATE TABLE `meta_data_auth` (
  `meta_data_auth_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请数据授权的ID',
  `user_identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请用户所属组织身份ID',
  `user_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请数据授权的用户ID',
  `user_type` int(11) NOT NULL COMMENT '用户类型 (0: 未定义; 1: 以太坊地址; 2: Alaya地址; 3: PlatON地址',
  `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '元数据ID,hash',
  `dfs_data_status` int(11) DEFAULT NULL COMMENT '元数据在分布式存储环境中的状态 (0: DataStatus_Unknown ; DataStatus_Normal = 1; DataStatus_Deleted = 2)',
  `dfs_data_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元数据在分布式存储环境中的ID',
  `auth_type` int(11) NOT NULL DEFAULT '0' COMMENT '申请收集授权类型：(0: 未定义; 1: 按照时间段来使用; 2: 按照次数来使用)',
  `start_at` datetime DEFAULT NULL COMMENT '授权开始时间(auth_type=1时)',
  `end_at` datetime DEFAULT NULL COMMENT '授权结束时间(auth_type=1时)',
  `times` int(11) DEFAULT '0' COMMENT '授权次数(auth_type=2时)',
  `expired` tinyint(1) DEFAULT '0' COMMENT '是否已过期 (当 usage_type 为 1 时才需要的字段)',
  `used_times` int(11) DEFAULT '0' COMMENT '已经使用的次数 (当 usage_type 为 2 时才需要的字段)',
  `apply_at` datetime(3) NOT NULL COMMENT '授权申请时间，精确到毫秒',
  `audit_option` int(11) DEFAULT '0' COMMENT '审核结果，0：等待审核中；1：审核通过；2：审核拒绝',
  `audit_desc` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '审核意见 (允许""字符)',
  `audit_at` datetime(3) DEFAULT NULL COMMENT '授权审核时间，精确到毫秒',
  `auth_sign` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权签名hex',
  `auth_status` int(11) DEFAULT '0' COMMENT '数据授权信息的状态 (0: 未知; 1: 还未发布的数据授权; 2: 已发布的数据授权; 3: 已撤销的数据授权 <失效前主动撤回的>; 4: 已经失效的数据授权 <过期or达到使用上限的>)',
  `update_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`meta_data_auth_id`),
  KEY `update_at` (`update_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据文件授权信息';

-- ----------------------------
-- Table structure for meta_data_option_part
-- ----------------------------
DROP TABLE IF EXISTS `meta_data_option_part`;
CREATE TABLE `meta_data_option_part` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'metaDataId',
  `meta_data_option_part` text COLLATE utf8mb4_unicode_ci COMMENT 'metaDataOption分片存储,顺序存储',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for org_info
-- ----------------------------
DROP TABLE IF EXISTS `org_info`;
CREATE TABLE `org_info` (
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份认证标识的id',
  `identity_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份认证标识的类型 (ca 或者 did)',
  `org_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织身份名称',
  `node_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '组织节点ID',
  `image_url` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织机构图像url',
  `profile` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织机构简介',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态,1:Normal;2:NonNormal',
  `accumulative_data_file_count` int(11) DEFAULT '0' COMMENT '组织的文件累积数量',
  `update_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '(状态)修改时间',
  PRIMARY KEY (`identity_id`),
  KEY `update_at` (`update_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织信息';

-- ----------------------------
-- Table structure for power_server
-- ----------------------------
DROP TABLE IF EXISTS `power_server`;
CREATE TABLE `power_server` (
  `id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计算服务主机ID,hash',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '组织身份ID',
  `memory` bigint(20) NOT NULL DEFAULT '0' COMMENT '计算服务内存, 字节',
  `core` int(11) NOT NULL DEFAULT '0' COMMENT '计算服务core',
  `bandwidth` bigint(20) NOT NULL DEFAULT '0' COMMENT '计算服务带宽, bps',
  `used_memory` bigint(20) DEFAULT '0' COMMENT '使用的内存, 字节',
  `used_core` int(11) DEFAULT '0' COMMENT '使用的core',
  `used_bandwidth` bigint(20) DEFAULT '0' COMMENT '使用的带宽, bps',
  `published` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否发布，true/false',
  `published_at` datetime(3) NOT NULL COMMENT '发布时间，精确到毫秒',
  `status` int(11) DEFAULT NULL COMMENT '算力的状态 (0: 未知; 1: 还未发布的算力; 2: 已发布的算力; 3: 已撤销的算力)',
  `update_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '(状态)修改时间',
  PRIMARY KEY (`id`),
  KEY `update_at` (`update_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计算服务信息';

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `task_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `user_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起任务的用户的信息 (task是属于用户的)',
  `user_type` int(11) NOT NULL COMMENT '用户类型 (0: 未定义; 1: 以太坊地址; 2: Alaya地址; 3: PlatON地址',
  `required_memory` bigint(20) NOT NULL DEFAULT '0' COMMENT '需要的内存, 字节',
  `required_core` int(11) NOT NULL DEFAULT '0' COMMENT '需要的core',
  `required_bandwidth` bigint(20) NOT NULL DEFAULT '0' COMMENT '需要的带宽, bps',
  `required_duration` bigint(20) NOT NULL DEFAULT '0' COMMENT '需要的时间, milli seconds',
  `owner_identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务创建者组织身份ID',
  `owner_party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  `create_at` datetime(3) NOT NULL COMMENT '任务创建时间，精确到毫秒',
  `start_at` datetime(3) DEFAULT NULL COMMENT '任务开始执行时间，精确到毫秒',
  `end_at` datetime(3) DEFAULT NULL COMMENT '任务结束时间，精确到毫秒',
  `used_memory` bigint(20) NOT NULL DEFAULT '0' COMMENT '使用的内存, 字节',
  `used_core` int(11) NOT NULL DEFAULT '0' COMMENT '使用的core',
  `used_bandwidth` bigint(20) NOT NULL DEFAULT '0' COMMENT '使用的带宽, bps',
  `used_file_size` bigint(20) DEFAULT '0' COMMENT '使用的所有数据大小，字节',
  `status` int(11) DEFAULT NULL COMMENT '任务状态, 0:未知;1:等待中;2:计算中,3:失败;4:成功',
  `status_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务状态说明',
  `remarks` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务描述',
  `task_sign` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务签名',
  PRIMARY KEY (`id`),
  KEY `end_at` (`end_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务';

-- ----------------------------
-- Table structure for task_algo_provider
-- ----------------------------
DROP TABLE IF EXISTS `task_algo_provider`;
CREATE TABLE `task_algo_provider` (
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '算法提供者组织身份ID',
  `party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务算法提供者';

-- ----------------------------
-- Table structure for task_event
-- ----------------------------
DROP TABLE IF EXISTS `task_event`;
CREATE TABLE `task_event` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `event_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产生事件的组织身份ID',
  `party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产生事件的partyId (单个组织可以担任任务的多个party, 区分是哪一方产生的event)',
  `event_at` datetime(3) NOT NULL COMMENT '产生事件的时间，精确到毫秒',
  `event_content` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件内容',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务事件';

-- ----------------------------
-- Table structure for task_meta_data
-- ----------------------------
DROP TABLE IF EXISTS `task_meta_data`;
CREATE TABLE `task_meta_data` (
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参与任务的元数据ID',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '(冗余)参与任务的元数据的所属组织的identity_id',
  `party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  `key_column_idx` int(11) DEFAULT NULL COMMENT '元数据在此次任务中的主键列下标索引序号',
  PRIMARY KEY (`task_id`,`meta_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务metadata';

-- ----------------------------
-- Table structure for task_meta_data_column
-- ----------------------------
DROP TABLE IF EXISTS `task_meta_data_column`;
CREATE TABLE `task_meta_data_column` (
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参与任务的元数据ID',
  `selected_column_idx` int(11) NOT NULL COMMENT '元数据在此次任务中的参与计算的字段索引序号',
  PRIMARY KEY (`task_id`,`meta_data_id`,`selected_column_idx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务metadata明细';

-- ----------------------------
-- Table structure for task_power_provider
-- ----------------------------
DROP TABLE IF EXISTS `task_power_provider`;
CREATE TABLE `task_power_provider` (
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '算力提供者组织身份ID',
  `party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  `used_memory` bigint(20) DEFAULT '0' COMMENT '任务使用的内存, 字节',
  `used_core` int(11) DEFAULT '0' COMMENT '任务使用的core',
  `used_bandwidth` bigint(20) DEFAULT '0' COMMENT '任务使用的带宽, bps',
  PRIMARY KEY (`task_id`,`identity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务算力提供者';

-- ----------------------------
-- Table structure for task_result_consumer
-- ----------------------------
DROP TABLE IF EXISTS `task_result_consumer`;
CREATE TABLE `task_result_consumer` (
  `task_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID,hash',
  `consumer_identity_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果消费者组织身份ID',
  `consumer_party_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  `producer_identity_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结果产生者的组织身份ID',
  `producer_party_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务参与方在本次任务中的唯一识别ID',
  PRIMARY KEY (`task_id`,`consumer_identity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务结果接收者';

-- ----------------------------
-- View structure for v_data_file_stats_daily
-- ----------------------------
-- DROP VIEW IF EXISTS `v_data_file_stats_daily`;
-- CREATE VIEW `v_data_file_stats_daily` AS select `a`.`stats_time` AS `stats_time`,`a`.`day_size` AS `day_size`,sum(`b`.`day_size`) AS `accu_size` from (((select cast(`df`.`published_at` as date) AS `stats_time`,sum(`df`.`size`) AS `day_size` from `metis_storage`.`data_file` `df` where (`df`.`status` = 2) group by cast(`df`.`published_at` as date) order by cast(`df`.`published_at` as date))) `a` join (select cast(`df`.`published_at` as date) AS `stats_time`,sum(`df`.`size`) AS `day_size` from `metis_storage`.`data_file` `df` where (`df`.`status` = 2) group by cast(`df`.`published_at` as date) order by cast(`df`.`published_at` as date)) `b` on((`a`.`stats_time` >= `b`.`stats_time`))) group by `a`.`stats_time` order by `a`.`stats_time` ;

-- ----------------------------
-- View structure for v_data_file_stats_monthly
-- ----------------------------
-- DROP VIEW IF EXISTS `v_data_file_stats_monthly`;
-- CREATE VIEW `v_data_file_stats_monthly` AS select `a`.`stats_time` AS `stats_time`,`a`.`month_size` AS `month_size`,sum(`b`.`month_size`) AS `accu_size` from (((select date_format(`df`.`published_at`,'%Y-%m') AS `stats_time`,sum(`df`.`size`) AS `month_size` from `metis_storage`.`data_file` `df` where (`df`.`status` = 2) group by date_format(`df`.`published_at`,'%Y-%m') order by date_format(`df`.`published_at`,'%Y-%m'))) `a` join (select date_format(`df`.`published_at`,'%Y-%m') AS `stats_time`,sum(`df`.`size`) AS `month_size` from `metis_storage`.`data_file` `df` where (`df`.`status` = 2) group by date_format(`df`.`published_at`,'%Y-%m') order by date_format(`df`.`published_at`,'%Y-%m')) `b` on((`a`.`stats_time` >= `b`.`stats_time`))) group by `a`.`stats_time` order by `a`.`stats_time` ;

-- ----------------------------
-- View structure for v_global_stats
-- ----------------------------
-- DROP VIEW IF EXISTS `v_global_stats`;
-- CREATE VIEW `v_global_stats` AS select `allOrg`.`total_org_count` AS `total_org_count`,`powerOrg`.`power_org_count` AS `power_org_count`,`srcFile`.`data_file_size` AS `data_file_size`,`usedFile`.`used_data_file_size` AS `used_data_file_size`,`task`.`task_count` AS `task_count`,(`partner`.`partner_count` + `task`.`task_count`) AS `partner_count`,`power`.`total_core` AS `total_core`,`power`.`total_memory` AS `total_memory`,`power`.`total_bandwidth` AS `total_bandwidth` from (((((((select count(0) AS `total_org_count` from `metis_storage`.`org_info` where (`metis_storage`.`org_info`.`status` = 1)) `allOrg` join (select count(`oi`.`identity_id`) AS `power_org_count` from `metis_storage`.`org_info` `oi` where (exists(select 1 from `metis_storage`.`power_server` `ps` where ((`oi`.`identity_id` = `ps`.`identity_id`) and (`ps`.`status` in (2,3)))) and (`oi`.`status` = 1))) `powerOrg`) join (select ifnull(sum(`metis_storage`.`data_file`.`size`),0) AS `data_file_size` from `metis_storage`.`data_file` where (`metis_storage`.`data_file`.`status` = 2)) `srcFile`) join (select ifnull(sum(`df`.`size`),0) AS `used_data_file_size` from (`metis_storage`.`task_meta_data` `tmd` left join `metis_storage`.`data_file` `df` on((`tmd`.`meta_data_id` = `df`.`meta_data_id`)))) `usedFile`) join (select count(0) AS `task_count` from `metis_storage`.`task`) `task`) join (select (((ifnull(`dataPartner`.`dataPartnerCount`,0) + ifnull(`powerPartner`.`powerPartnerCount`,0)) + ifnull(`algoPartner`.`algoPartnerCount`,0)) + ifnull(`resultConsumerPartner`.`resultConsumerPartnerCount`,0)) AS `partner_count` from ((((select count(0) AS `dataPartnerCount` from `metis_storage`.`task_meta_data`) `dataPartner` join (select count(0) AS `powerPartnerCount` from `metis_storage`.`task_power_provider`) `powerPartner`) join (select count(0) AS `algoPartnerCount` from `metis_storage`.`task_algo_provider`) `algoPartner`) join (select count(0) AS `resultConsumerPartnerCount` from `metis_storage`.`task_result_consumer`) `resultConsumerPartner`)) `partner`) join (select ifnull(sum(`p`.`core`),0) AS `total_core`,ifnull(sum(`p`.`memory`),0) AS `total_memory`,ifnull(sum(`p`.`bandwidth`),0) AS `total_bandwidth` from (`metis_storage`.`power_server` `p` join `metis_storage`.`org_info` `o` on((`p`.`identity_id` = `o`.`identity_id`))) where ((`o`.`status` = 1) and (`p`.`status` in (2,3)))) `power`) ;

-- ----------------------------
-- View structure for v_org_daily_task_stats
-- ----------------------------
-- DROP VIEW IF EXISTS `v_org_daily_task_stats`;
-- CREATE VIEW `v_org_daily_task_stats` AS select `tmp`.`identity_id` AS `identity_id`,count(`tmp`.`task_id`) AS `task_count`,cast(`t`.`create_at` as date) AS `task_date` from ((select `oi`.`identity_id` AS `identity_id`,`t`.`id` AS `task_id` from (`metis_storage`.`org_info` `oi` join `metis_storage`.`task` `t`) where (`oi`.`identity_id` = `t`.`owner_identity_id`) union select `oi`.`identity_id` AS `identity_id`,`tap`.`task_id` AS `task_id` from (`metis_storage`.`org_info` `oi` join `metis_storage`.`task_algo_provider` `tap`) where (`oi`.`identity_id` = `tap`.`identity_id`) union select `oi`.`identity_id` AS `identity_id`,`tpp`.`task_id` AS `task_id` from (`metis_storage`.`org_info` `oi` join `metis_storage`.`task_power_provider` `tpp`) where (`oi`.`identity_id` = `tpp`.`identity_id`) union select `oi`.`identity_id` AS `identity_id`,`tmd`.`task_id` AS `task_id` from (`metis_storage`.`org_info` `oi` join `metis_storage`.`task_meta_data` `tmd`) where (`oi`.`identity_id` = `tmd`.`identity_id`) union select `oi`.`identity_id` AS `identity_id`,`trc`.`task_id` AS `task_id` from (`metis_storage`.`org_info` `oi` join `metis_storage`.`task_result_consumer` `trc`) where (`oi`.`identity_id` = `trc`.`consumer_identity_id`)) `tmp` join `metis_storage`.`task` `t`) where (`tmp`.`task_id` = `t`.`id`) group by `tmp`.`identity_id`,`task_date` ;

-- ----------------------------
-- View structure for v_power_stats_daily
-- ----------------------------
-- DROP VIEW IF EXISTS `v_power_stats_daily`;
-- CREATE VIEW `v_power_stats_daily` AS select `a`.`stats_time` AS `stats_time`,`a`.`day_core` AS `day_core`,`a`.`day_memory` AS `day_memory`,`a`.`day_bandwidth` AS `day_bandwidth`,sum(`b`.`day_core`) AS `accu_core`,sum(`b`.`day_memory`) AS `accu_memory`,sum(`b`.`day_bandwidth`) AS `accu_bandwidth` from (((select cast(`ps`.`published_at` as date) AS `stats_time`,sum(`ps`.`core`) AS `day_core`,sum(`ps`.`memory`) AS `day_memory`,sum(`ps`.`bandwidth`) AS `day_bandwidth` from `metis_storage`.`power_server` `ps` where ((`ps`.`status` = 2) or (`ps`.`status` = 3)) group by cast(`ps`.`published_at` as date) order by cast(`ps`.`published_at` as date))) `a` join (select cast(`ps`.`published_at` as date) AS `stats_time`,sum(`ps`.`core`) AS `day_core`,sum(`ps`.`memory`) AS `day_memory`,sum(`ps`.`bandwidth`) AS `day_bandwidth` from `metis_storage`.`power_server` `ps` where ((`ps`.`status` = 2) or (`ps`.`status` = 3)) group by cast(`ps`.`published_at` as date) order by cast(`ps`.`published_at` as date)) `b` on((`a`.`stats_time` >= `b`.`stats_time`))) group by `a`.`stats_time` order by `a`.`stats_time` ;

-- ----------------------------
-- View structure for v_power_stats_monthly
-- ----------------------------
-- DROP VIEW IF EXISTS `v_power_stats_monthly`;
-- CREATE VIEW `v_power_stats_monthly` AS select `a`.`stats_time` AS `stats_time`,`a`.`month_core` AS `month_core`,`a`.`month_memory` AS `month_memory`,`a`.`month_bandwidth` AS `month_bandwidth`,sum(`b`.`month_core`) AS `accu_core`,sum(`b`.`month_memory`) AS `accu_memory`,sum(`b`.`month_bandwidth`) AS `accu_bandwidth` from (((select date_format(`ps`.`published_at`,'%Y-%m') AS `stats_time`,sum(`ps`.`core`) AS `month_core`,sum(`ps`.`memory`) AS `month_memory`,sum(`ps`.`bandwidth`) AS `month_bandwidth` from `metis_storage`.`power_server` `ps` where ((`ps`.`status` = 2) or (`ps`.`status` = 3)) group by date_format(`ps`.`published_at`,'%Y-%m') order by date_format(`ps`.`published_at`,'%Y-%m'))) `a` join (select date_format(`ps`.`published_at`,'%Y-%m') AS `stats_time`,sum(`ps`.`core`) AS `month_core`,sum(`ps`.`memory`) AS `month_memory`,sum(`ps`.`bandwidth`) AS `month_bandwidth` from `metis_storage`.`power_server` `ps` where ((`ps`.`status` = 2) or (`ps`.`status` = 3)) group by date_format(`ps`.`published_at`,'%Y-%m') order by date_format(`ps`.`published_at`,'%Y-%m')) `b` on((`a`.`stats_time` >= `b`.`stats_time`))) group by `a`.`stats_time` order by `a`.`stats_time` ;
