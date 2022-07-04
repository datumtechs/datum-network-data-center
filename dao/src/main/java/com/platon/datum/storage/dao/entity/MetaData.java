package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Author liushuyu
 * @Date 2022/4/11 17:42
 * @Version
 * @Desc
 */

@Getter
@Setter
@ToString
public class MetaData {

    //元数据ID,hash
    private String metaDataId;
    //组织身份ID
    private String identityId;
    //预留
    private String dataId;
    //元数据在分布式存储环境中的状态 (0: DataStatus_Unknown ; DataStatus_Normal = 1; DataStatus_Deleted = 2)默认是1
    private Integer dataStatus;
    //元数据的名称 (对外的表名)
    private String metaDataName;
    //表示该元数据是 `普通数据` 还是 `模型数据` 的元数据 (0: 未定义; 1: 普通数据元数据; 2: 模型数据元数据)默认是1
    private Integer metaDataType;
    //原始数据的Hash (如果是 远端数据源, 则为 资源Id 的Hash, 如: url的Hash)
    private String dataHash;
    //元数据的摘要(描述)
    private String desc;
    //源数据的存储位置类型 (组织本地服务器、远端服务器、云等)：0-未知，1-存储在组织本地服务器上，2-存储在远端服务器上 默认是1
    private Integer locationType;
    //源数据的类型 (目前只有 csv)：0-未知，1-CSV 默认是1
    private Integer dataType;
    //元数据所属的行业 (有用户自定义, 仅做展示用)
    private String industry;
    //元数据的状态 (0: 未知; 1: 还未发布的新表; 2: 已发布的表; 3: 已撤销的表)
    private Integer status;
    //发布时间，精确到毫秒
    private LocalDateTime publishAt;
    //(状态)修改时间
    private LocalDateTime updateAt;
    //元数据的 nonce (用来标识该元数据在所属组织中的元数据的序号, 从 0 开始递增)
    private Long nonce;
    //元数据的选项，和 data_type 配套使用
    private String metaDataOption;
    //元数据的拥有者地址
    private String user;
    //元数据的拥有者地址对应账户类型 0-未定义, 1-第二地址, 2-测试网地址, 3-主网地址
    private Integer userType;
}
