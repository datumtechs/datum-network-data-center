package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Author juzix
 * @Date 2022/4/28 14:59
 * @Version
 * @Desc ******************************
 */

/**
 * 元数据文件授权信息
 */
@Getter
@Setter
@ToString
public class MetaDataAuth {
    /**
     * 元数据授权申请Id
     */
    private String metaDataAuthId;

    /**
     * 申请人地址
     */
    private String user;

    /**
     * 预留
     */
    private String dataId;

    /**
     * 1- valid, 2 - invalid.
     */
    private Integer dataStatus;

    /**
     * 申请人类型 (0: 未定义; 1: 以太坊地址; 2: Alaya地址; 3: PlatON地址
     */
    private Integer userType;

    /**
     * 元数据所属的组织信息
     */
    private String identityId;

    /**
     * 元数据ID,hash
     */
    private String metaDataId;

    /**
     * 元数据的使用方式 (0: 未定义; 1: 按照时间段来使用; 2: 按照次数来使用)
     */
    private Integer usageType;

    /**
     * 可使用的开始时间 (当 usage_type 为 1 时才需要的字段)
     */
    private LocalDateTime startAt;

    /**
     * 可使用的结束时间 (当 usage_type 为 1 时才需要的字段)
     */
    private LocalDateTime endAt;

    /**
     * 可使用的次数 (当 usage_type 为 2 时才需要的字段)
     */
    private Integer times;

    /**
     * 审核结果，0：等待审核中；1：审核通过；2：审核拒绝
     */
    private Integer auditOption;

    /**
     * 审核意见 (允许""字符)
     */
    private String auditSuggestion;

    /**
     * 是否已过期 (当 usage_type 为 1 时才需要的字段)
     */
    private Integer expire;

    /**
     * 已经使用的次数 (当 usage_type 为 2 时才需要的字段)
     */
    private Integer usedTimes;

    /**
     * 发起授权申请的时间 (单位: ms)
     */
    private LocalDateTime applyAt;

    /**
     * 审核授权申请的时间 (单位: ms)
     */
    private LocalDateTime auditAt;

    /**
     * 数据授权信息的状态 (0: 未知; 1: 还未发布的数据授权; 2: 已发布的数据授权; 3: 已撤销的数据授权 <失效前主动撤回的>; 4: 已经失效的数据授权 <过期or达到使用上限的>)
     */
    private Integer state;

    private String sign;

    /**
     * 数据发布时间
     */
    private LocalDateTime publishAt;

    /**
     * 数据更新时间
     */
    private LocalDateTime updateAt;

    /**
     * 元数据授权的 nonce (用来标识该元数据授权在所属组织中的元数据授权的序号, 从 0 开始递增)
     */
    private Long nonce;
}