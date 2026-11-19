-- OTD-WMS：交付库入库闭环（OTD-WMS-DSN-CR-003）
-- ============================================================
-- 变更内容：
--   1) tb_inventory：新增 target_warehouse_code、lifecycle_seq；state 增加 4=IN_TRANSIT；
--      IN_TRANSIT 期间不占储位（storage_area_code/storage_location_code 为 NULL）。
--   2) tb_inbound_order：新增 lifecycle_seq（本次入库属于该 VIN 第几段）。
--   3) 新建 tb_inbound_task（交付库待入库任务）。
-- 幂等：全部使用 information_schema 守卫，可在部分执行后重跑。
-- 存量：既有行 lifecycle_seq 默认 1，无需回填 IN_TRANSIT。
-- ============================================================

-- ------------------------------------------------------------
-- 1. tb_inventory 扩展
-- ------------------------------------------------------------
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND COLUMN_NAME = 'target_warehouse_code') = 0,
               'ALTER TABLE `tb_inventory` ADD COLUMN `target_warehouse_code` VARCHAR(50) NULL COMMENT ''目标交付库代码（IN_TRANSIT 期间有效）'' AFTER `reserved_order_num`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND COLUMN_NAME = 'lifecycle_seq') = 0,
               'ALTER TABLE `tb_inventory` ADD COLUMN `lifecycle_seq` INT NOT NULL DEFAULT 1 COMMENT ''已发生的入库段次'' AFTER `target_warehouse_code`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- state 注释补充 IN_TRANSIT（仅当旧注释缺失时执行）
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND COLUMN_NAME = 'state'
                  AND COLUMN_COMMENT NOT LIKE '%IN_TRANSIT%') > 0,
               'ALTER TABLE `tb_inventory` MODIFY COLUMN `state` INT NOT NULL COMMENT ''状态：1-AVAILABLE，2-OUTBOUND，3-RESERVED，4-IN_TRANSIT''',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. tb_inbound_order 扩展
-- ------------------------------------------------------------
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inbound_order'
                  AND COLUMN_NAME = 'lifecycle_seq') = 0,
               'ALTER TABLE `tb_inbound_order` ADD COLUMN `lifecycle_seq` INT NOT NULL DEFAULT 1 COMMENT ''本次入库属于该 VIN 第几段'' AFTER `pre_order_num`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3. tb_inbound_task（交付库待入库任务）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_inbound_task`
(
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`              VARCHAR(255) NOT NULL COMMENT '待入库任务号',
    `request_id`             VARCHAR(64)  NOT NULL COMMENT '到货请求幂等标识',
    `source_system`          VARCHAR(50)  NOT NULL COMMENT '通用来源系统',
    `source_order_num`       VARCHAR(255) NOT NULL COMMENT '来源单号',
    `vin`                    VARCHAR(20)  NOT NULL COMMENT '车架号',
    `build_config_code`      VARCHAR(255) NOT NULL COMMENT '生产配置代码（来自当前库存快照）',
    `warehouse_code`         VARCHAR(50)  NOT NULL COMMENT '目标交付库代码',
    `state`                  VARCHAR(32)  NOT NULL COMMENT '任务状态：PENDING/ARRIVED/COMPLETED/CANCELLED',
    `estimated_arrival_time` TIMESTAMP             DEFAULT NULL COMMENT '预计到达时间',
    `arrival_time`           TIMESTAMP             DEFAULT NULL COMMENT '到达时间',
    `arrival_by`             BIGINT                DEFAULT NULL COMMENT '到达确认人',
    `inbound_time`           TIMESTAMP             DEFAULT NULL COMMENT '入库完成时间',
    `inbound_by`             BIGINT                DEFAULT NULL COMMENT '入库确认人',
    `cancel_time`            TIMESTAMP             DEFAULT NULL COMMENT '取消时间',
    `cancel_by`              BIGINT                DEFAULT NULL COMMENT '取消人',
    `cancel_reason`          VARCHAR(255)          DEFAULT NULL COMMENT '取消原因',
    `lifecycle_seq`          INT          NOT NULL DEFAULT 1 COMMENT '目标生命周期段次',
    `aggregate_version`      BIGINT       NOT NULL DEFAULT 1 COMMENT '任务聚合版本',
    `description`            VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`              BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`              BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`            INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`              TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inbound_task_num` (`order_num`),
    UNIQUE KEY `uk_inbound_task_source_request` (`source_system`, `request_id`),
    UNIQUE KEY `uk_inbound_task_source_order_vin` (`source_system`, `source_order_num`, `vin`),
    KEY `idx_inbound_task_warehouse_state` (`warehouse_code`, `state`),
    KEY `idx_inbound_task_vin` (`vin`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='交付库待入库任务';
