USE `datum_storage`;

ALTER TABLE `meta_data`
    DROP COLUMN `allow_expose`,
    DROP COLUMN `token_address`,
    ADD COLUMN `user` VARCHAR(200) NOT NULL   COMMENT '元数据的拥有者地址' AFTER `nonce`,
    ADD COLUMN `user_type` TINYINT NOT NULL   COMMENT '元数据的拥有者地址对应账户类型 0-未定义, 1-第二地址, 2-测试网地址, 3-主网地址' AFTER `user`;

ALTER TABLE`org_info`
    CHANGE `identity_type` `identity_type` TINYINT NOT NULL   COMMENT '身份认证标识的类型 0-未知, 1-DID';
