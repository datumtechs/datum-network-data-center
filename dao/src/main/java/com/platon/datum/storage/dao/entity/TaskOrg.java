package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Author juzix
 * @Date 2022/4/21 19:54
 * @Version
 * @Desc ******************************
 */

/**
 * 任务组织信息
 */
@Getter
@Setter
@ToString
public class TaskOrg {
    /**
     * 关联的任务ID
     */
    private String taskId;

    /**
     * 任务中的所处的角色：1-任务的发起方，2-任务的算法提供者，3-任务的数据提供方,4-任务的算力提供方,5-任务的结果接收方
     * {@link TaskRoleEnum}
     */
    private Integer taskRole;

    private String partyId;

    /**
     * 组织名称
     */
    private String nodeName;

    /**
     * 组织节点ID
     */
    private String nodeId;

    /**
     * 组织identityID
     */
    private String identityId;

    @Getter
    @ToString
    public enum TaskRoleEnum {
        sender(1, "任务的发起方"),
        algoSupplier(2, "任务的算法提供者"),
        dataSupplier(3, "任务的数据提供方"),
        powerSupplier(4, "任务的算力提供方"),
        receiver(5, "任务的结果接收方"),
        ;

        TaskRoleEnum(int role,String desc) {
            this.role = role;
            this.desc = desc;
        }

        private int role;
        private String desc;

        public static TaskRoleEnum getRoleEnum(int role){
            Optional<TaskRoleEnum> first = Arrays.stream(TaskRoleEnum.values())
                    .filter(taskRoleEnum -> taskRoleEnum.getRole() == role)
                    .findFirst();
            return first.get();
        }
    }
}