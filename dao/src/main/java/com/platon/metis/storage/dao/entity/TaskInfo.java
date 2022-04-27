package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version
 * @Desc ******************************
 */

/**
 * 任务信息表
 */
@Getter
@Setter
@ToString
public class TaskInfo {
    /**
     * 任务Id
     */
    private String taskId;

    /**
     * 预留
     */
    private String dataId;

    /**
     * the status of data for local storage, 1 means valid, 2 means invalid.
     */
    private Integer dataStatus;

    /**
     * 发起任务的用户的信息 (task是属于用户的)
     */
    private String user;

    /**
     * 用户类型 (0: 未定义; 1: 第二地址; 2: 测试网地址; 3: 主网地址)
     */
    private Integer userType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务的数据提供方选择数据策略的类型
     */
    private String dataPolicyTypes;

    /**
     * 任务的算力提供方选择算力策略的类型
     */
    private String powerPolicyTypes;

    /**
     * 任务的数据流向策略的类型
     */
    private String dataFlowPolicyTypes;

    /**
     * 算法元数据Id (为了后续支持 算法市场而用, 使用内置算法时则该值为 "" 空字符串)
     */
    private String metaAlgorithmId;

    /**
     * 任务的状态 (0: 未知; 1: 等在中; 2: 计算中; 3: 失败; 4: 成功)
     */
    private Integer state;

    /**
     * 任务失败原因
     */
    private String reason;

    /**
     * 任务描述(非必须)
     */
    private String desc;

    /**
     * 任务的发起时间戳 (单位: ms)
     */
    private LocalDateTime createAt;

    /**
     * 任务的开始执行时间戳 (单位: ms)
     */
    private LocalDateTime startAt;

    /**
     * 任务的终止<成功or失败>时间戳 (单位: ms)
     */
    private LocalDateTime endAt;

    /**
     * 消息签名 (userType 和 user决定算法)
     */
    private String sign;

    /**
     * 任务的 nonce (用来标识该任务在任务发起方组织中的任务的序号, 从 0 开始递增)
     */
    private Long nonce;

    /**
     * 任务的初始声明的所需内存
     */
    private Long initMemory;

    /**
     * 任务的初始声明的所需cpu
     */
    private Integer initProcessor;

    /**
     * 任务的初始声明的所需带宽
     */
    private Long initBandwidth;

    /**
     * 任务的初始声明的所需任务时长
     */
    private Long initDuration;

    public List<Integer> getDataPolicyTypesList() {
        String[] split = dataPolicyTypes.split(",");
        return Arrays.asList(split).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public void setDataPolicyTypes(List<Integer> dataPolicyTypes) {
        StringJoiner sj = new StringJoiner(",");
        dataPolicyTypes.forEach(dataPolicyTypes1 -> {
            sj.add(dataPolicyTypes1.toString());
        });
        this.dataPolicyTypes = sj.toString();
    }

    public List<Integer> getPowerPolicyTypesList() {
        String[] split = powerPolicyTypes.split(",");
        return Arrays.asList(split).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public void setPowerPolicyTypes(List<Integer> powerPolicyTypes) {
        StringJoiner sj = new StringJoiner(",");
        powerPolicyTypes.forEach(powerPolicyTypes1 -> {
            sj.add(powerPolicyTypes1.toString());
        });
        this.powerPolicyTypes = sj.toString();
    }

    public List<Integer> getDataFlowPolicyTypesList() {
        String[] split = dataFlowPolicyTypes.split(",");
        return Arrays.asList(split).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public void setDataFlowPolicyTypes(List<Integer> dataFlowPolicyTypes) {
        StringJoiner sj = new StringJoiner(",");
        dataFlowPolicyTypes.forEach(dataFlowPolicyTypes1 -> {
            sj.add(dataFlowPolicyTypes1.toString());
        });
        this.dataFlowPolicyTypes = sj.toString();
    }
}