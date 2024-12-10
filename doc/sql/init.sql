DROP TABLE IF EXISTS `db_wms`.`tb_warehouse`;
CREATE TABLE `db_wms`.`tb_warehouse`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`            VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `name`            VARCHAR(255) NOT NULL COMMENT '仓库名称',
    `manager`         VARCHAR(64)           DEFAULT NULL COMMENT '仓库管理员',
    `warehouse_level` VARCHAR(20)  NOT NULL COMMENT '仓库体系层级：PDC-前置库，CDC-中央库，RDC-区域库，DDC-经销商库',
    `province_code`   VARCHAR(20)           DEFAULT NULL COMMENT '省级行政区代码',
    `city_code`       VARCHAR(20)           DEFAULT NULL COMMENT '地区级行政区代码',
    `county_code`     VARCHAR(20)           DEFAULT NULL COMMENT '县级行政区代码',
    `address`         VARCHAR(255)          DEFAULT NULL COMMENT '仓库地址',
    `zipcode`         VARCHAR(50)           DEFAULT NULL COMMENT '邮编',
    `fax`             VARCHAR(50)           DEFAULT NULL COMMENT '传真',
    `tel`             VARCHAR(50)           DEFAULT NULL COMMENT '电话',
    `mobile`          VARCHAR(50)           DEFAULT NULL COMMENT '手机',
    `enable`          TINYINT      NOT NULL COMMENT '是否启用',
    `sort`            INT          NOT NULL COMMENT '排序',
    `description`     VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`       BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`       BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`     INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`       TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='整车仓库';

DROP TABLE IF EXISTS `db_wms`.`tb_storage_area`;
CREATE TABLE `db_wms`.`tb_storage_area`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`           VARCHAR(50)  NOT NULL COMMENT '储区代码',
    `name`           VARCHAR(255) NOT NULL COMMENT '储区名称',
    `warehouse_code` VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `manager`        VARCHAR(64)           DEFAULT NULL COMMENT '储区管理员',
    `enable`         TINYINT      NOT NULL COMMENT '是否启用',
    `sort`           INT          NOT NULL COMMENT '排序',
    `description`    VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`      BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`      BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`    INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`      TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='整车储区';

DROP TABLE IF EXISTS `db_wms`.`tb_storage_location`;
CREATE TABLE `db_wms`.`tb_storage_location`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`              VARCHAR(50)  NOT NULL COMMENT '储位代码',
    `name`              VARCHAR(255) NOT NULL COMMENT '储位名称',
    `warehouse_code`    VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `storage_area_code` VARCHAR(50)  NOT NULL COMMENT '储区代码',
    `manager`           VARCHAR(64)           DEFAULT NULL COMMENT '储位管理员',
    `enable`            TINYINT      NOT NULL COMMENT '是否启用',
    `sort`              INT          NOT NULL COMMENT '排序',
    `description`       VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='整车储位';