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
                             `meta_data_option` text COLLATE utf8mb4_unicode_ci COMMENT '元数据的选项，和 data_type 配套使用',
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

