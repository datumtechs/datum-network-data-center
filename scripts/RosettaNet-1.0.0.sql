drop database if exists dev_rosettanet;

CREATE DATABASE dev_rosettanet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dev_rosettanet;

-- 组织计算资源的使用率是动态的，当有任务正在计算时才有意义。
DROP TABLE IF EXISTS org_info;
CREATE TABLE org_info (
	identity_id VARCHAR(128) NOT NULL COMMENT '身份认证标识的id',
	identity_type VARCHAR(100) NOT NULL COMMENT '身份认证标识的类型 (ca 或者 did)',
    org_name VARCHAR(100) COMMENT '组织身份名称',
    node_id VARCHAR(128) NOT NULL COMMENT '组织身份ID',
    status VARCHAR(10) NOT NULL DEFAULT 'enabled' COMMENT '状态,enabled/disabled',
    accumulative_core INT DEFAULT 0 COMMENT '组织算力累积的core数量',
    accumulative_memory BIGINT DEFAULT 0 COMMENT '组织算力累积的内存数量, 字节',
    accumulative_bandwidth BIGINT DEFAULT 0 COMMENT '组织算力累积的带宽数量, bps',
    accumulative_power_task_count INT DEFAULT 0 COMMENT '组织作为算力提供方参与的任务累积数量',
    accumulative_data_task_count INT DEFAULT 0 COMMENT '组织作为数据提供方参与的任务累积数量',
    accumulative_data_file_count INT DEFAULT 0 COMMENT '组织的文件累积数量',
    PRIMARY KEY (identity_id)
) comment '组织信息';

DROP TABLE IF EXISTS schedule_server;
CREATE TABLE schedule_server (
    id VARCHAR(128) NOT NULL COMMENT '调度服务主机ID,hash',
    identity_id VARCHAR(128) NOT NULL COMMENT '组织身份ID',
	internal_ip VARCHAR(100) NOT NULL COMMENT '调度服务节点的内网 ip',
	internal_port VARCHAR(10) NOT NULL COMMENT '调度服务节点的内网 port',
    status  VARCHAR(20) NOT NULL COMMENT '节点状态，enabled:可用; disabled:不可用',
	PRIMARY KEY (id),
	UNIQUE KEY (identity_id)
) comment '调度服务';

DROP TABLE IF EXISTS data_server;
CREATE TABLE data_server (
    id VARCHAR(128) NOT NULL comment '数据服务主机ID,hash',
    identity_id VARCHAR(128) NOT NULL COMMENT '组织身份ID',
    server_name VARCHAR(100) NOT NULL COMMENT '数据服务名称',
	internal_ip VARCHAR(100)  COMMENT '数据服务的内网 ip',
	internal_port VARCHAR(10) COMMENT '数据服务的内网 port',
	external_ip VARCHAR(100) COMMENT '数据服务的外网 ip',
	external_port VARCHAR(10)  COMMENT '数据服务的内网 port用',
    remarks VARCHAR(100) COMMENT '备注信息',
    published BOOLEAN NOT NULL COMMENT '是否发布，true/false',
    published_at DATETIME NOT NULL DEFAULT NOW() comment '发布时间',
    status  VARCHAR(20) NOT NULL COMMENT '数据服务主机状态，enabled:可用; disabled:不可用',
    PRIMARY KEY (id),
	UNIQUE KEY (identity_id, server_name)
) comment '数据服务信息';

DROP TABLE IF EXISTS power_server;
CREATE TABLE power_server (
    id VARCHAR(128) NOT NULL comment '计算服务主机ID,hash',
    identity_id VARCHAR(128) NOT NULL COMMENT '组织身份ID',
    server_name VARCHAR(100) NOT NULL COMMENT '计算服务名称',
	internal_ip VARCHAR(100) COMMENT '计算服务的内网 ip',
	internal_port VARCHAR(10) COMMENT '计算服务的内网 port',
	external_ip VARCHAR(100) COMMENT '计算服务的外网 ip',
	external_port VARCHAR(10) COMMENT '计算服务的内网 port用',
    remarks VARCHAR(100) COMMENT '备注信息',
    memory BIGINT  NOT NULL DEFAULT 0 COMMENT '计算服务内存, 字节',
    core INT NOT NULL DEFAULT 0 COMMENT '计算服务core',
    bandwidth BIGINT  NOT NULL DEFAULT 0 COMMENT '计算服务带宽, bps',
    used_memory BIGINT DEFAULT 0 COMMENT '使用的内存, 字节',
    used_core INT DEFAULT 0 COMMENT '使用的core',
    used_bandwidth BIGINT DEFAULT 0 COMMENT '使用的带宽, bps',
    published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否发布，true/false',
    published_at DATETIME NOT NULL DEFAULT NOW() comment '发布时间',
    status  VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '计算服务主机状态，enabled:可用; disabled:不可用',
    PRIMARY KEY (id),
	UNIQUE KEY (identity_id, server_name)
) comment '计算服务信息';


DROP TABLE IF EXISTS data_file;
CREATE TABLE data_file (
    id VARCHAR(128) NOT NULL comment '数据文件ID,hash',
    identity_id VARCHAR(128) NOT NULL COMMENT '组织身份ID',
    file_name VARCHAR(100) NOT NULL COMMENT '文件名称',
    file_path VARCHAR(100) NOT NULL COMMENT '文件存储路径',
    file_type VARCHAR(20) NOT NULL COMMENT '文件后缀/类型, csv',
    resource_name VARCHAR(100) NOT NULL COMMENT '资源名称',
    size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `rows` BIGINT NOT NULL DEFAULT 0  COMMENT '数据行数(不算title)',
    columns INT NOT NULL DEFAULT 0  COMMENT '数据列数',
    published BOOLEAN NOT NULL DEFAULT false comment '是否公开发布的，true/false',
    published_at DATETIME NOT NULL DEFAULT NOW() comment '发布时间',
    has_title BOOLEAN NOT NULL DEFAULT false comment '是否带标题',
    remarks VARCHAR(100) COMMENT '数据描述',
    status VARCHAR(20) NOT NULL DEFAULT 'created' COMMENT '数据的状态 (created: 还未发布的新表; released: 已发布的表; revoked: 已撤销的表)',
    meta_data_id VARCHAR(128) NOT NULL comment '元数据ID,hash',
	PRIMARY KEY (id),
    UNIQUE KEY (meta_data_id),
	UNIQUE KEY (identity_id, resource_name)
) comment '数据文件信息';

DROP TABLE IF EXISTS meta_data_column;
CREATE TABLE meta_data_column (
    meta_data_id VARCHAR(128) NOT NULL comment '元数据ID,hash',
    column_idx int NOT NULL COMMENT '字段索引序号',
    column_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    column_type VARCHAR(100) NOT NULL COMMENT '字段类型',
    remarks VARCHAR(100) COMMENT '字段描述',
    published BOOLEAN NOT NULL DEFAULT TRUE comment '是否公开的, true/false',
    PRIMARY KEY(meta_data_id, column_idx)
) comment '数据文件元数据信息';

DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id VARCHAR(128) NOT NULL comment '任务ID,hash',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    required_memory BIGINT NOT NULL DEFAULT 0 COMMENT '需要的内存, 字节',
    required_core INT NOT NULL DEFAULT 0 COMMENT '需要的core',
    required_bandwidth BIGINT NOT NULL DEFAULT 0 COMMENT '需要的带宽, bps',
    required_duration BIGINT NOT NULL DEFAULT 0 COMMENT '需要的时间, milli seconds',
    owner_identity_id VARCHAR(100) NOT NULL COMMENT '任务创建者组织身份ID',
    create_at DATETIME NOT NULL COMMENT '任务创建时间',
    start_at DATETIME COMMENT '任务开始执行时间',
    end_at DATETIME COMMENT '任务结束时间',
    used_memory BIGINT NOT NULL DEFAULT 0 COMMENT '使用的内存, 字节',
    used_core INT NOT NULL DEFAULT 0 COMMENT '使用的core',
    used_bandwidth BIGINT NOT NULL DEFAULT 0 COMMENT '使用的带宽, bps',
    used_file_size BIGINT  DEFAULT 0 COMMENT '使用的所有数据大小，字节',
    status VARCHAR(20) NOT NULL COMMENT '任务状态, pending/denied/computing/failed/success',
    PRIMARY KEY (ID)
) comment '任务';

DROP TABLE IF EXISTS task_meta_data;
CREATE TABLE task_meta_data (
    task_id VARCHAR(128) NOT NULL comment '任务ID,hash',
    meta_data_id VARCHAR(128) NOT NULL COMMENT '参与任务的元数据ID',
    PRIMARY KEY (task_ID, meta_data_ID)
) comment '任务metadata';

DROP TABLE IF EXISTS task_meta_data_column;
CREATE TABLE task_meta_data_column (
    task_id VARCHAR(128) NOT NULL comment '任务ID,hash',
    meta_data_id VARCHAR(128) NOT NULL COMMENT '参与任务的元数据ID',
    column_idx int NOT NULL COMMENT '字段索引序号',
    PRIMARY KEY (task_ID, meta_data_ID,column_idx)
) comment '任务metadata明细';


DROP TABLE IF EXISTS task_power_provider;
CREATE TABLE task_power_provider(
    task_id VARCHAR(128) NOT NULL comment '任务ID,hash',
    identity_id VARCHAR(128) NOT NULL COMMENT '算力提供者组织身份ID',
    used_memory BIGINT DEFAULT 0 COMMENT '任务使用的内存, 字节',
    used_core INT DEFAULT 0 COMMENT '任务使用的core',
    used_bandwidth BIGINT DEFAULT 0 COMMENT '任务使用的带宽, bps',
    PRIMARY KEY (task_ID, identity_id)
) comment '任务算力提供者';


DROP TABLE IF EXISTS task_result_consumer;
CREATE TABLE task_result_consumer (
    task_id VARCHAR(128) NOT NULL comment '任务ID,hash',
    consumer_identity_id VARCHAR(128) NOT NULL COMMENT '结果消费者组织身份ID',
    producer_identity_id VARCHAR(128) NOT NULL COMMENT '结果产生者的组织身份ID',
    PRIMARY KEY (task_ID, consumer_identity_id, producer_identity_id)
) comment '任务结果接收者';


DROP TABLE IF EXISTS task_event;
CREATE TABLE task_event (
    ID BIGINT auto_increment NOT NULL comment 'ID',
    task_id VARCHAR(128) NOT NULL comment '任务ID,hash',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型',
    identity_id VARCHAR(128) NOT NULL COMMENT '产生事件的组织身份ID',
    event_at DATETIME NOT NULL COMMENT '产生事件的时间',
    event_content VARCHAR(512) NOT NULL COMMENT '事件内容',
    PRIMARY KEY (ID)
) comment '任务事件';


-- 算力增长统计表
DROP TABLE IF EXISTS power_change_history;
CREATE TABLE power_change_history (
    id VARCHAR(128) NOT NULL comment 'power_provider.ID,hash',
    memory BIGINT  NOT NULL DEFAULT 0 COMMENT '计算服务内存, 字节，如果可用->不可用，则为负数',
    core INT NOT NULL DEFAULT 0 COMMENT '计算服务core',
    bandwidth BIGINT  NOT NULL DEFAULT 0 COMMENT '计算服务带宽, bps',
    status  VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '计算服务主机状态，enabled:可用; disabled:不可用',
    trend VARCHAR(10) NOT NULL COMMENT 'increased:增长的/reduced:减少的',
    update_at DATE NOT NULL comment '修改时间',
	INDEX (update_at)
) comment '算力增长统计表';

-- 数据增长统计表
DROP TABLE IF EXISTS data_file_change_history;
CREATE TABLE data_file_change_history (
    id VARCHAR(128) NOT NULL comment 'data_file.ID,hash',
    size BIGINT  NOT NULL DEFAULT 0 COMMENT '文件大小(字节)，如果released->revoked，则为负数',
    status VARCHAR(20) NOT NULL DEFAULT 'create' COMMENT '数据的状态 (created: 还未发布的新表; released: 已发布的表; revoked: 已撤销的表)',
    trend VARCHAR(10) NOT NULL COMMENT 'increased:增长的/reduced:减少的',
    update_at DATE NOT NULL comment '修改时间',
	INDEX (update_at)
) comment '数据增长统计表';

---------------------------------------------


-- 统计 org 提供算力的任务累积数量
DELIMITER $$
drop trigger if exists task_power_provider_insert_trigger $$
CREATE trigger task_power_provider_insert_trigger AFTER INSERT ON task_power_provider FOR EACH Row
begin
    update org_info
    set accumulative_power_task_count  = ifnull(accumulative_power_task_count,0) + 1
    where identity_id = NEW.identity_id;
end
$$
DELIMITER ;

-- 统计 org 提供数据的任务累积数量
DELIMITER $$
drop trigger if exists task_meta_data_insert_trigger $$
CREATE trigger task_meta_data_insert_trigger AFTER INSERT ON task_meta_data FOR EACH Row
begin
    update org_info
    set accumulative_data_task_count  = ifnull(accumulative_data_task_count,0) + 1
    where identity_id = (select identity_id from data_file where meta_data_id = NEW.meta_data_id);
end
$$
DELIMITER ;

-- 统计 org 提供的数据文件的累积数量
-- 统计 data_file数量, data_file_size总数
DELIMITER $$
drop trigger if exists data_file_insert_trigger $$
CREATE trigger data_file_insert_trigger AFTER INSERT ON data_file FOR EACH Row
begin
    insert into data_file_change_history (id, size, trend, status, update_at)
    values (NEW.id, NEW.size, NEW.status, 'increased', CURRENT_DATE());

    update org_info
    set accumulative_data_file_count  = ifnull(accumulative_data_file_count,0) + 1
    where identity_id = NEW.identity_id;
end
$$
DELIMITER ;

-- 统计 power_server 数量
-- 统计 org 提供的算力的累积数量
DELIMITER $$
drop trigger if exists power_server_insert_trigger $$
CREATE trigger power_server_insert_trigger AFTER INSERT ON power_server FOR EACH Row
begin
    insert into power_change_history (id, memory, core, bandwidth, status, trend, update_at)
    values (NEW.id, NEW.memory, NEW.core, NEW.bandwidth, NEW.status, 'increased', CURRENT_DATE());

    update org_info
    set accumulative_core  =  ifnull(accumulative_core,0) + NEW.core, accumulative_memory  = ifnull(accumulative_memory,0) + NEW.memory, accumulative_bandwidth  = ifnull(accumulative_bandwidth,0) + NEW.bandwidth
    where identity_id = NEW.identity_id;
end
$$
DELIMITER ;

DELIMITER $$
drop trigger if exists power_server_update_trigger $$
CREATE trigger power_server_update_trigger AFTER UPDATE ON power_server FOR EACH Row
begin
    IF OLD.status='enabled' AND NEW.status='disabled' THEN
        insert into power_change_history (id, memory, core, bandwidth, status, trend, update_at)
        values (NEW.id, 0-OLD.memory, 0-OLD.core, 0-OLD.bandwidth, 'disabled', 'reduced', CURRENT_DATE());
    ELSEIF OLD.status='disabled' AND NEW.status='enabled' THEN
        insert into power_change_history (id, memory, core, bandwidth, status, trend, update_at)
        values (OLD.id, OLD.memory, OLD.core, OLD.bandwidth, 'enabled', 'increased', CURRENT_DATE());
    END IF;
end
$$
DELIMITER ;






DELIMITER $$
drop trigger if exists data_file_update_trigger $$
CREATE trigger data_file_update_trigger AFTER UPDATE ON data_file FOR EACH Row
begin
    IF OLD.status='released' AND NEW.status='revoked' THEN
        insert into data_file_change_history (id, size, status, trend, update_at)
        values (NEW.id, 0-OLD.size, 'revoked', 'reduced', CURRENT_DATE());
    ELSEIF OLD.status='revoked' AND NEW.status='released' THEN
        insert into data_file_change_history (id, size, trend, update_at)
        values (OLD.id, OLD.size, 'released', 'increased', CURRENT_DATE());
    END IF;
end
$$
DELIMITER ;


-- 创建首页统计 view
create or replace view v_global_stats as
select dataServer.data_server_count, powerServer.power_server_count, srcFile.data_file_size, usedFile.used_data_file_size,
    task.task_count, partner.partner_count, power.total_core, power.total_memory, power.total_bandwidth
from
-- 数据host数
(
    select count(*) as data_server_count
    from data_server where status='enabled'
) dataServer,

-- 算力host数
(
    select count(*) as power_server_count
    from power_server where status='enabled'
) powerServer,

-- 上传数据量
(
    select IFNULL(sum(size),0) as data_file_size
    from data_file where status='released'
) srcFile,

-- 交易数据量
(
    select ifnull(sum(df.size),0) as used_data_file_size
    from task_meta_data tmd
    left join data_file df on tmd.meta_data_id = df.meta_data_id
) usedFile,

-- 完成任务数
(
    select count(*) as task_count
    from task
) task,

-- 参与任务任务总人数
(
	select ifnull(dataPartner.dataPartnerCount,0) + ifnull(powerPartner.powerPartnerCount,0) as partner_count
	from
	(
		select count(*) as dataPartnerCount
		from task_meta_data
	) dataPartner,
	(
		select count(*) as powerPartnerCount
		from task_power_provider
	) powerPartner
) as partner,

-- 总算力
(
    select ifnull(sum(p.core),0) as total_core, ifnull(sum(p.memory),0) as total_memory, ifnull(sum(p.bandwidth),0) as total_bandwidth
	from power_server p
) as power;

-- 首页算力走势统计 view
create or replace view v_power_trend_stats as
SELECT
	a1.update_at, a1.daily_memory, a1.daily_core, a1.daily_bandwidth,
	sum( a2.daily_memory ) total_memory, sum( a2.daily_core ) total_core , sum( a2.daily_bandwidth ) total_bandwidth
FROM
(
	SELECT update_at, sum( memory) as daily_memory , sum(core) as daily_core, sum(bandwidth) as daily_bandwidth
	FROM power_change_history
	GROUP BY update_at
) a1
LEFT JOIN
(
	SELECT update_at, sum( memory) as daily_memory , sum(core) as daily_core, sum(bandwidth) as daily_bandwidth
	FROM power_change_history
	GROUP BY update_at
) a2 ON a1.update_at >= a2.update_at
GROUP BY a1.update_at
ORDER BY a1.update_at;

/*create or replace view v_power_trend_stats as
SELECT t.*, (@i:=@i+t.dailyMemory) as totalMemory, (@j:=@j+t.dailyCore) as totalCore, (@k:=@k+t.dailyBandwidth) as totalBandwidth
FROM
(
	SELECT update_at, sum( memory) as dailyMemory , sum(core) as dailyCore, sum(bandwidth) as dailyBandwidth
	FROM power_change_history
	GROUP BY update_at
) t, (select @i := 0, @j :=0, @k :=0) temp;*/

-- 首页数据量走势统计 view
create or replace view v_data_file_trend_stats as
SELECT
	a1.update_at, a1.daily_size,
	sum( a2.daily_size ) total_size
FROM
(
	SELECT update_at, sum(size) as daily_size
    FROM data_file_change_history
    GROUP BY update_at
) a1
LEFT JOIN
(
	SELECT update_at, sum(size) as daily_size
    FROM data_file_change_history
    GROUP BY update_at
) a2 ON a1.update_at >= a2.update_at
GROUP BY a1.update_at
ORDER BY a1.update_at;

/*create or replace view v_data_file_trend_stats as
SELECT t.*, (@i:=@i+t.dailySize) as totalSize
FROM
(
    SELECT update_at, sum(size) as dailySize
    FROM data_file_change_history
    GROUP BY update_at
) t, (select @i := 0) temp;*/