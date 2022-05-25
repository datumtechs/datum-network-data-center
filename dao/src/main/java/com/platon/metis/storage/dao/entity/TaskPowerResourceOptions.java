package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author juzix
 * @Date 2022/4/22 16:11
 * @Version 
 * @Desc 
 *******************************
 */
/**
    * 算力的资源消耗明细
    */
@Getter
@Setter
@ToString
public class TaskPowerResourceOptions {
    /**
    * 主键ID
    */
    private Integer id;

    /**
    * 关联得任务ID
    */
    private String taskId;

    private String partId;

    /**
    * 服务系统的总内存 (单位: byte)
    */
    private Long totalMemory;

    /**
    * 服务的总内核数 (单位: 个)
    */
    private Integer totalProcessor;

    /**
    * 服务的总带宽数 (单位: bps)
    */
    private Long totalBandwidth;

    /**
    * 服务的总磁盘空间 (单位: byte)
    */
    private Long totalDisk;

    /**
    * 服务系统的已用内存  (单位: byte)
    */
    private Long usedMemory;

    /**
    * 服务的已用内核数 (单位: 个)
    */
    private Integer usedProcessor;

    /**
    * 服务的已用带宽数 (单位: bps)
    */
    private Long usedBandwidth;

    /**
    * 服务的已用磁盘空间 (单位: byte)
    */
    private Long usedDisk;
}