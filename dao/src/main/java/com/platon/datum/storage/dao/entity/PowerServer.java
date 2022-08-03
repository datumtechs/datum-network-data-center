package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Author juzix
 * @Date 2022/4/27 10:45
 * @Version
 * @Desc
 *******************************
 */

/**
 * 计算服务信息
 */
@Getter
@Setter
@ToString
public class PowerServer extends BaseDomain {
    /**
     * 算力的powerId
     */
    private String dataId;

    /**
     * 组织身份ID
     */
    private String identityId;

    /**
     * 1 - valid, 2 - invalid.
     */
    private Integer dataStatus;

    /**
     * 算力的状态 (0: 未知; 1: create 还未发布的算力; 2: release 已发布的算力; 3: revoke 已撤销的算力)
     */
    private Integer state;

    /**
     * 算力总内存 (单位: byte)
     */
    private Long totalMem;

    /**
     * 算力已使用内存 (单位: byte)
     */
    private Long usedMem;

    /**
     * 算力总内核数 (单位: 个)
     */
    private Integer totalProcessor;

    /**
     * 算力已使用内核数 (单位: 个)
     */
    private Integer usedProcessor;

    /**
     * 算力总带宽数 (单位: bps)
     */
    private Long totalBandwidth;

    /**
     * 算力已使用带宽数 (单位: bps)
     */
    private Long usedBandwidth;

    /**
     * 算力总磁盘容量 (单位: byte)
     */
    private Long totalDisk;

    /**
     * 算力已使用磁盘容量 (单位: byte)
     */
    private Long usedDisk;

    /**
     * 数据发布时间
     */
    private LocalDateTime publishAt;

    /**
     * (状态)修改时间
     */
    private LocalDateTime updateAt;

    /**
     * 算力的 nonce (用来标识该算力在所属组织中的算力的序号, 从 0 开始递增)
     */
    private Long nonce;
}
