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

DROP TABLE IF EXISTS `db_wms`.`tb_pre_inbound_order`;
CREATE TABLE `db_wms`.`tb_pre_inbound_order`
(
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`              VARCHAR(255) NOT NULL COMMENT '预入库单号',
    `vin`                    VARCHAR(20)  NOT NULL COMMENT '车架号',
    `model_config_code`      VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`         VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `estimated_arrival_time` TIMESTAMP             DEFAULT NULL COMMENT '预计到达时间',
    `estimated_inbound_time` TIMESTAMP             DEFAULT NULL COMMENT '预计入库时间',
    `audit`                  TINYINT               DEFAULT 0 COMMENT '是否已审核',
    `audit_time`             TIMESTAMP             DEFAULT NULL COMMENT '审核时间',
    `audit_by`               BIGINT                DEFAULT NULL COMMENT '审核者',
    `arrival`                TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已到达',
    `arrival_time`           TIMESTAMP             DEFAULT NULL COMMENT '到达时间',
    `inbound`                TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已入库',
    `inbound_time`           TIMESTAMP             DEFAULT NULL COMMENT '入库时间',
    `inbound_by`             BIGINT                DEFAULT NULL COMMENT '入库者',
    `description`            VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`              BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`              BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`            INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`              TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='预入库单';

DROP TABLE IF EXISTS `db_wms`.`tb_inbound_order`;
CREATE TABLE `db_wms`.`tb_inbound_order`
(
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`             VARCHAR(255) NOT NULL COMMENT '入库单号',
    `pre_order_num`         VARCHAR(255)          DEFAULT NULL COMMENT '预入库单号',
    `vin`                   VARCHAR(20)  NOT NULL COMMENT '车架号',
    `model_config_code`     VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`        VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `storage_area_code`     VARCHAR(50)           DEFAULT NULL COMMENT '储区代码',
    `storage_location_code` VARCHAR(50)           DEFAULT NULL COMMENT '储位代码',
    `inbound_time`          TIMESTAMP    NOT NULL COMMENT '入库时间',
    `inbound_by`            BIGINT                DEFAULT NULL COMMENT '入库者',
    `description`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`             BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`             BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`           INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`             TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='入库单';

DROP TABLE IF EXISTS `db_wms`.`tb_inventory`;
CREATE TABLE `db_wms`.`tb_inventory`
(
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vin`                   VARCHAR(20)  NOT NULL COMMENT '车架号',
    `model_config_code`     VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`        VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `storage_area_code`     VARCHAR(50)           DEFAULT NULL COMMENT '储区代码',
    `storage_location_code` VARCHAR(50)           DEFAULT NULL COMMENT '储位代码',
    `inbound_time`          TIMESTAMP             DEFAULT NULL COMMENT '入库时间',
    `inbound_by`            BIGINT                DEFAULT NULL COMMENT '入库者',
    `outbound_time`         TIMESTAMP             DEFAULT NULL COMMENT '出库时间',
    `outbound_by`           BIGINT                DEFAULT NULL COMMENT '出库者',
    `state`                 SMALLINT              DEFAULT NULL COMMENT '库存状态：1-在库，2-出库',
    `description`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`             BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`             BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`           INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`             TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_warehouse_code` (`warehouse_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='库存';

DROP TABLE IF EXISTS `db_wms`.`tb_inventory_transfer`;
CREATE TABLE `db_wms`.`tb_inventory_transfer`
(
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vin`                       VARCHAR(20)  NOT NULL COMMENT '车架号',
    `model_config_code`         VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`            VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `old_storage_area_code`     VARCHAR(50)           DEFAULT NULL COMMENT '原储区代码',
    `old_storage_location_code` VARCHAR(50)           DEFAULT NULL COMMENT '原储位代码',
    `new_storage_area_code`     VARCHAR(50)  NOT NULL COMMENT '新储区代码',
    `new_storage_location_code` VARCHAR(50)  NOT NULL COMMENT '新储位代码',
    `transfer_state`            SMALLINT              DEFAULT NULL COMMENT '移库状态',
    `transfer_time`             TIMESTAMP             DEFAULT NULL COMMENT '移库时间',
    `transfer_by`               BIGINT                DEFAULT NULL COMMENT '移库者',
    `description`               VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`                 BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`                 BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`               INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`                 TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_warehouse_code` (`warehouse_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='移库';

DROP TABLE IF EXISTS `db_wms`.`tb_inventory_count`;
CREATE TABLE `db_wms`.`tb_inventory_count`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`         VARCHAR(255) NOT NULL COMMENT '盘点单号',
    `type`              SMALLINT     NOT NULL COMMENT '盘点类型：1-按仓库，2-按储区',
    `model_config_code` VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`    VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `storage_area_code` VARCHAR(50)           DEFAULT NULL COMMENT '储区代码',
    `state`             SMALLINT     NOT NULL COMMENT '盘点状态：1-新建，2-已下发，3-开始盘点，4-结束盘点',
    `start_time`        TIMESTAMP             DEFAULT NULL COMMENT '开始盘点时间',
    `end_time`          TIMESTAMP             DEFAULT NULL COMMENT '结束盘点时间',
    `description`       VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_num`),
    KEY `idx_warehouse_code` (`warehouse_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='盘点';

DROP TABLE IF EXISTS `db_wms`.`tb_inventory_count_detail`;
CREATE TABLE `db_wms`.`tb_inventory_count_detail`
(
    `id`                           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`                    VARCHAR(255) NOT NULL COMMENT '盘点单号',
    `vin`                          VARCHAR(20)  NOT NULL COMMENT '车架号',
    `model_config_code`            VARCHAR(255) NOT NULL COMMENT '车型配置代码',
    `warehouse_code`               VARCHAR(50)  NOT NULL COMMENT '仓库代码',
    `storage_area_code`            VARCHAR(50)           DEFAULT NULL COMMENT '储区代码',
    `storage_location_code`        VARCHAR(50)           DEFAULT NULL COMMENT '储位代码',
    `actual_warehouse_code`        VARCHAR(50)           DEFAULT NULL COMMENT '实际仓库代码',
    `actual_storage_area_code`     VARCHAR(50)           DEFAULT NULL COMMENT '实际储区代码',
    `actual_storage_location_code` VARCHAR(50)           DEFAULT NULL COMMENT '实际储位代码',
    `count_time`                   TIMESTAMP             DEFAULT NULL COMMENT '盘点时间',
    `description`                  VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`                    BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`                    BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`                  INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`                    TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_order_num` (`order_num`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='盘点明细';