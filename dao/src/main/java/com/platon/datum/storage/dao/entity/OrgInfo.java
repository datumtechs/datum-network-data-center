package com.platon.datum.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class OrgInfo {
    private Integer identityType;//身份认证标识的类型 (ca 或者 did)
    private String identityId;//身份认证标识的id
    private String nodeId;//组织节点ID
    private String nodeName;//组织身份名称
    private String dataId;//预留
    private Integer dataStatus;//1 - valid, 2 - invalid
    private Integer status;//1 - valid, 2 - invalid
    private String credential;//json format for credential
    private String imageUrl;//组织机构图像url
    private String details;//组织机构简介
    private Long nonce;//身份信息的 nonce (用来标识该身份在所属组织中发布的序号, 从 0 开始递增; 注: 身份可以来回发布注销，所以nonce表示第几次发布)
    private LocalDateTime updateAt;//更新时间
}
