package com.platon.metis.storage.dao;

import com.platon.metis.storage.dao.entity.MetaDataAuth;

/**
 * @Author juzix
 * @Date 2022/4/28 14:59
 * @Version 
 * @Desc 
 *******************************
 */
public interface MetaDataAuthMapper {
    /**
     * delete by primary key
     * @param metaDataAuthId primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(String metaDataAuthId);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(MetaDataAuth record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(MetaDataAuth record);

    /**
     * select by primary key
     * @param metaDataAuthId primary key
     * @return object by primary key
     */
    MetaDataAuth selectByPrimaryKey(String metaDataAuthId);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(MetaDataAuth record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(MetaDataAuth record);
}