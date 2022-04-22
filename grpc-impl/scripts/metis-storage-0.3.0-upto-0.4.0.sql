-- drop database if exists `metis_storage`;

-- CREATE DATABASE `metis_storage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `metis_storage`;

drop table data_file;
drop table meta_data_column;

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

CREATE TABLE `meta_data_option_part` (
                                         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
                                         `meta_data_id` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'metaDataId',
                                         `meta_data_option_part` text COLLATE utf8mb4_unicode_ci COMMENT 'metaDataOption分片存储,顺序存储',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

DROP VIEW IF EXISTS `v_data_file_stats_daily`;

DROP VIEW IF EXISTS `v_data_file_stats_monthly`;

DROP VIEW IF EXISTS `v_global_stats`;

DROP VIEW IF EXISTS `v_org_daily_task_stats`;

DROP VIEW IF EXISTS `v_power_stats_daily`;

DROP VIEW IF EXISTS `v_power_stats_monthly`;

DROP TABLE IF EXISTS `task`;

DROP TABLE IF EXISTS `task_algo_provider`;

DROP TABLE IF EXISTS `task_meta_data`;

DROP TABLE IF EXISTS `task_meta_data_column`;

DROP TABLE IF EXISTS `task_power_provider`;

DROP TABLE IF EXISTS `task_result_consumer`;


CREATE TABLE `task_data_flow_option_part` (
                                              `id` int NOT NULL,
                                              `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联的任务ID',
                                              `data_flow_option_part` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '数据片段',
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务的数据流向策略的内容 (json字符串, 和 data_flow_policy_type 配套使用)';

CREATE TABLE `task_data_option_part` (
                                         `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联的任务ID',
                                         `data_option_part` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '分片后的信息',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务的数据提供方选择数据策略的内容 (json字符串, 和 data_policy_type 配套使用)';


CREATE TABLE `task_info` (
                             `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务Id',
                             `data_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预留',
                             `data_status` tinyint DEFAULT NULL COMMENT 'the status of data for local storage, 1 means valid, 2 means invalid.',
                             `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发起任务的用户的信息 (task是属于用户的)',
                             `user_type` tinyint DEFAULT NULL COMMENT '用户类型 (0: 未定义; 1: 第二地址; 2: 测试网地址; 3: 主网地址)',
                             `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务名称',
                             `data_policy_type` tinyint DEFAULT NULL COMMENT '任务的数据提供方选择数据策略的类型',
                             `power_policy_type` tinyint DEFAULT NULL COMMENT '任务的算力提供方选择算力策略的类型',
                             `data_flow_policy_type` tinyint DEFAULT NULL COMMENT '任务的数据流向策略的类型',
                             `meta_algorithm_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '算法元数据Id (为了后续支持 算法市场而用, 使用内置算法时则该值为 "" 空字符串)',
                             `state` tinyint DEFAULT NULL COMMENT '任务的状态 (0: 未知; 1: 等在中; 2: 计算中; 3: 失败; 4: 成功)',
                             `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务失败原因',
                             `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务描述(非必须)',
                             `create_at` datetime(3) DEFAULT NULL COMMENT '任务的发起时间戳 (单位: ms)',
                             `start_at` datetime(3) DEFAULT NULL COMMENT '任务的开始执行时间戳 (单位: ms)',
                             `end_at` datetime(3) DEFAULT NULL COMMENT '任务的终止<成功or失败>时间戳 (单位: ms)',
                             `sign` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消息签名 (userType 和 user决定算法)',
                             `nonce` int DEFAULT NULL COMMENT '任务的 nonce (用来标识该任务在任务发起方组织中的任务的序号, 从 0 开始递增)',
                             `init_memory` bigint DEFAULT NULL COMMENT '任务的初始声明的所需内存',
                             `init_processor` int DEFAULT NULL COMMENT '任务的初始声明的所需cpu',
                             `init_bandwidth` bigint DEFAULT NULL COMMENT '任务的初始声明的所需带宽',
                             `init_duration` int DEFAULT NULL COMMENT '任务的初始声明的所需任务时长',
                             PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务信息表';

CREATE TABLE `task_inner_algorithm_code_part` (
                                                  `id` int NOT NULL,
                                                  `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联的任务ID',
                                                  `algorithm_code_part` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内置算法代码 (使用内置算法, 在不使用算法市场前提下用)',
                                                  `algorithm_code_extra_params_part` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内置算法的额外超参 (使用内置算法, 内置算法的额外超参数 json 字符串, 内容可为""空字符串, 根据算法来)',
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务内置算法代码';

CREATE TABLE `task_org` (
                            `task_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联的任务ID',
                            `task_role` int NOT NULL COMMENT '任务中的所处的角色：1-任务的发起方，2-任务的算法提供者，3-任务的数据提供方,4-任务的算力提供方,5-任务的结果接收方',
                            `party_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `node_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织名称',
                            `node_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织节点ID\n',
                            `identity_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织identityID',
                            UNIQUE KEY `uk_task_role_identity` (`task_id`,`task_role`,`identity_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务组织信息';

CREATE TABLE `task_power_option_part` (
                                          `id` int NOT NULL,
                                          `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联的任务ID',
                                          `power_option_part` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '数据片段',
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务的算力提供方选择算力策略的内容 (json字符串, 和 power_policy_type 配套使用)';

CREATE TABLE `task_power_resource_option` (
                                              `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联得任务ID',
                                              `part_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                              `total_memory` bigint DEFAULT NULL COMMENT '服务系统的总内存 (单位: byte)',
                                              `total_processor` int DEFAULT NULL COMMENT '服务的总内核数 (单位: 个)',
                                              `total_bandwidth` bigint DEFAULT NULL COMMENT '服务的总带宽数 (单位: bps)',
                                              `total_disk` bigint DEFAULT NULL COMMENT '服务的总磁盘空间 (单位: byte)',
                                              `used_memory` bigint DEFAULT NULL COMMENT '服务系统的已用内存  (单位: byte)',
                                              `used_processor` int DEFAULT NULL COMMENT '服务的已用内核数 (单位: 个)',
                                              `used_bandwidth` bigint DEFAULT NULL COMMENT '服务的已用带宽数 (单位: bps)',
                                              `used_disk` bigint DEFAULT NULL COMMENT '服务的已用磁盘空间 (单位: byte)',
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算力的资源消耗明细';

