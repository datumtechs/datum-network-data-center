package com.platon.metis.storage.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author liushuyu
 * @Date 2022/4/11 19:12
 * @Version
 * @Desc
 */


@Getter
@Setter
@ToString
public class MetaDataOptionPart {

    //自增ID
    private int id;

    //metaDataId
    private String metaDataId;

    //metaDataOption分片存储,顺序存储
    private String metaDataOptionPart;
}
