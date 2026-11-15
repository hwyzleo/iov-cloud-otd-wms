-- =====================================================================
-- WMS-DSN-CR-001 基础闭环数据结构
-- 1) 预入库权威状态与来源字段、VIN 唯一
-- 2) 入库单预入库单号唯一
-- 3) 库存当前行唯一、储位占用唯一、预留字段
-- 4) 移储请求幂等标识
-- 5) 出库单/明细、Inbox/Outbox 四张新表
--
-- 幂等性：MySQL DDL 非事务，迁移中途失败会留下“部分执行”状态。本脚本对每个
-- 结构变更都用 information_schema 守卫，可在 flyway repair 后安全重跑补齐。
--
-- 迁移门禁：唯一约束在存量重复数据存在时会失败，不得静默合并车辆或储位。
-- 预检报告见 doc/sql/cr001_preflight.sql，清理脚本见 doc/sql/cr001_remediation_*.sql。
-- =====================================================================

-- ---------- 1. 预入库单 ----------

-- 1.1 state 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'state') = 0,
               'ALTER TABLE `tb_pre_inbound_order` ADD COLUMN `state` VARCHAR(32) NULL COMMENT ''权威状态：PENDING_ARRIVAL/ARRIVED/COMPLETED/CANCELLED'' AFTER `warehouse_code`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 source_system 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'source_system') = 0,
               'ALTER TABLE `tb_pre_inbound_order` ADD COLUMN `source_system` VARCHAR(50) NULL COMMENT ''来源系统；VMD 接入时为 VMD'' AFTER `state`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 source_event_id 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'source_event_id') = 0,
               'ALTER TABLE `tb_pre_inbound_order` ADD COLUMN `source_event_id` VARCHAR(64) NULL COMMENT ''首个来源事件ID'' AFTER `source_system`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 source_batch_num 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'source_batch_num') = 0,
               'ALTER TABLE `tb_pre_inbound_order` ADD COLUMN `source_batch_num` VARCHAR(255) NULL COMMENT ''来源批次号'' AFTER `source_event_id`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.5 存量状态由已验证的到达/入库事实回填，不虚构状态
UPDATE `tb_pre_inbound_order`
SET `state` = CASE
                  WHEN `inbound` = 1 THEN 'COMPLETED'
                  WHEN `arrival` = 1 THEN 'ARRIVED'
                  ELSE 'PENDING_ARRIVAL'
    END
WHERE `state` IS NULL;

-- 1.6 存量来源标记为 LEGACY，来源事件标识取唯一单号
UPDATE `tb_pre_inbound_order`
SET `source_system`   = 'LEGACY',
    `source_event_id` = CONCAT('LEGACY-', `order_num`)
WHERE `source_system` IS NULL
   OR `source_event_id` IS NULL;

-- 1.7 权威状态与来源字段收紧为非空
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'state' AND IS_NULLABLE = 'YES') > 0,
               'ALTER TABLE `tb_pre_inbound_order` MODIFY COLUMN `state` VARCHAR(32) NOT NULL COMMENT ''权威状态：PENDING_ARRIVAL/ARRIVED/COMPLETED/CANCELLED''',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'source_system' AND IS_NULLABLE = 'YES') > 0,
               'ALTER TABLE `tb_pre_inbound_order` MODIFY COLUMN `source_system` VARCHAR(50) NOT NULL COMMENT ''来源系统；VMD 接入时为 VMD''',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND COLUMN_NAME = 'source_event_id' AND IS_NULLABLE = 'YES') > 0,
               'ALTER TABLE `tb_pre_inbound_order` MODIFY COLUMN `source_event_id` VARCHAR(64) NOT NULL COMMENT ''首个来源事件ID''',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.8 本范围不支持退车回库：一个 VIN 一张预入库单
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pre_inbound_order'
                  AND INDEX_NAME = 'uk_pre_inbound_vin') = 0,
               'ALTER TABLE `tb_pre_inbound_order` ADD UNIQUE KEY `uk_pre_inbound_vin` (`vin`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 2. 入库单 ----------

-- 新流程中一张预入库单只能生成一张入库单（MySQL 唯一索引允许多个 NULL）
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inbound_order'
                  AND INDEX_NAME = 'uk_inbound_pre_order_num') = 0,
               'ALTER TABLE `tb_inbound_order` ADD UNIQUE KEY `uk_inbound_pre_order_num` (`pre_order_num`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 3. 库存 ----------

-- 3.1 reserved_order_num 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND COLUMN_NAME = 'reserved_order_num') = 0,
               'ALTER TABLE `tb_inventory` ADD COLUMN `reserved_order_num` VARCHAR(255) NULL COMMENT ''当前预留出库单号'' AFTER `state`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.2 出库库存按最终态规则不保留储位占用（仅收敛既有 OUTBOUND 行，不虚构 RESERVED）
UPDATE `tb_inventory`
SET `storage_area_code`     = NULL,
    `storage_location_code` = NULL
WHERE `state` = 2
  AND (`storage_area_code` IS NOT NULL
    OR `storage_location_code` IS NOT NULL);

-- 3.3 一个 VIN 一条当前库存
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND INDEX_NAME = 'uk_inventory_vin') = 0,
               'ALTER TABLE `tb_inventory` ADD UNIQUE KEY `uk_inventory_vin` (`vin`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.4 AVAILABLE/RESERVED 时储位唯一；OUTBOUND 清空储位后允许多条 NULL
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND INDEX_NAME = 'uk_inventory_occupied_location') = 0,
               'ALTER TABLE `tb_inventory` ADD UNIQUE KEY `uk_inventory_occupied_location` (`warehouse_code`, `storage_location_code`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.5 仓库 + 状态查询索引
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory'
                  AND INDEX_NAME = 'idx_inventory_warehouse_state') = 0,
               'ALTER TABLE `tb_inventory` ADD KEY `idx_inventory_warehouse_state` (`warehouse_code`, `state`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 4. 移储 ----------

-- 4.1 request_id 列
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory_transfer'
                  AND COLUMN_NAME = 'request_id') = 0,
               'ALTER TABLE `tb_inventory_transfer` ADD COLUMN `request_id` VARCHAR(64) NULL COMMENT ''移储请求幂等标识'' AFTER `warehouse_code`',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.2 存量请求标识回填
UPDATE `tb_inventory_transfer`
SET `request_id` = CONCAT('LEGACY-', `id`)
WHERE `request_id` IS NULL;

-- 4.3 收紧为非空
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory_transfer'
                  AND COLUMN_NAME = 'request_id' AND IS_NULLABLE = 'YES') > 0,
               'ALTER TABLE `tb_inventory_transfer` MODIFY COLUMN `request_id` VARCHAR(64) NOT NULL COMMENT ''移储请求幂等标识''',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.4 请求幂等唯一约束
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory_transfer'
                  AND INDEX_NAME = 'uk_inventory_transfer_request') = 0,
               'ALTER TABLE `tb_inventory_transfer` ADD UNIQUE KEY `uk_inventory_transfer_request` (`request_id`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.5 VIN 查询索引
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_inventory_transfer'
                  AND INDEX_NAME = 'idx_transfer_vin') = 0,
               'ALTER TABLE `tb_inventory_transfer` ADD KEY `idx_transfer_vin` (`vin`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 5. 出库单 ----------
CREATE TABLE IF NOT EXISTS `tb_outbound_order`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`         VARCHAR(255) NOT NULL COMMENT 'WMS 出库单号',
    `request_id`        VARCHAR(64)  NOT NULL COMMENT '首次出库请求幂等标识',
    `source_system`     VARCHAR(50)  NOT NULL COMMENT '通用来源系统',
    `source_order_num`  VARCHAR(255) NOT NULL COMMENT '来源单号',
    `warehouse_code`    VARCHAR(50)  NOT NULL COMMENT '已解析仓库代码',
    `state`             VARCHAR(32)  NOT NULL COMMENT '出库单状态：RESERVED/PARTIALLY_LOADED/LOADED/PARTIALLY_OUTBOUND/COMPLETED/CANCELLED',
    `aggregate_version` BIGINT       NOT NULL DEFAULT 1 COMMENT '出库聚合版本，初始为 1',
    `description`       VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outbound_order_num` (`order_num`),
    UNIQUE KEY `uk_outbound_source_request` (`source_system`, `request_id`),
    UNIQUE KEY `uk_outbound_source_order` (`source_system`, `source_order_num`),
    KEY `idx_outbound_warehouse_state` (`warehouse_code`, `state`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='出库单';

CREATE TABLE IF NOT EXISTS `tb_outbound_order_detail`
(
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`             VARCHAR(255) NOT NULL COMMENT 'WMS 出库单号',
    `vin`                   VARCHAR(20)  NOT NULL COMMENT '车架号',
    `build_config_code`     VARCHAR(255) NOT NULL COMMENT '生产配置代码',
    `warehouse_code`        VARCHAR(50)  NOT NULL COMMENT '预留时仓库快照',
    `storage_area_code`     VARCHAR(50)  NOT NULL COMMENT '预留时储区快照',
    `storage_location_code` VARCHAR(50)  NOT NULL COMMENT '预留时储位快照',
    `state`                 VARCHAR(32)  NOT NULL COMMENT '明细状态：RESERVED/LOADED/OUTBOUND/RELEASED',
    `reserve_time`          TIMESTAMP    NOT NULL COMMENT '预留时间',
    `reserve_by`            BIGINT                DEFAULT NULL COMMENT '预留者',
    `load_time`             TIMESTAMP             DEFAULT NULL COMMENT '装车时间',
    `load_by`               BIGINT                DEFAULT NULL COMMENT '装车者',
    `outbound_time`         TIMESTAMP             DEFAULT NULL COMMENT '出库时间',
    `outbound_by`           BIGINT                DEFAULT NULL COMMENT '出库者',
    `release_time`          TIMESTAMP             DEFAULT NULL COMMENT '取消释放时间',
    `release_by`            BIGINT                DEFAULT NULL COMMENT '取消释放者',
    `description`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`             BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`             BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`           INT                   DEFAULT NULL COMMENT '记录版本，用于条件流转',
    `row_valid`             TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outbound_detail_order_vin` (`order_num`, `vin`),
    KEY `idx_outbound_detail_vin` (`vin`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='出库单 VIN 明细';

-- ---------- 6. Inbox ----------
CREATE TABLE IF NOT EXISTS `tb_wms_inbox`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `consumer_name`   VARCHAR(100) NOT NULL COMMENT '消费者名称',
    `topic`           VARCHAR(255) NOT NULL COMMENT '来源 Topic',
    `event_id`        VARCHAR(64)  NOT NULL COMMENT '事件唯一 ID',
    `event_type`      VARCHAR(128) NOT NULL COMMENT '事件类型',
    `payload_digest`  VARCHAR(64)  NOT NULL COMMENT '规范化载荷摘要',
    `process_state`   VARCHAR(32)  NOT NULL COMMENT '处理状态：PROCESSED/BUSINESS_REJECTED/DEAD_LETTERED',
    `business_key`    VARCHAR(255)          DEFAULT NULL COMMENT '业务幂等键',
    `result_event_id` VARCHAR(64)           DEFAULT NULL COMMENT '对应结果事件 ID',
    `error_code`      VARCHAR(32)           DEFAULT NULL COMMENT '错误码',
    `error_message`   VARCHAR(2000)         DEFAULT NULL COMMENT '脱敏错误信息',
    `received_time`   TIMESTAMP    NOT NULL COMMENT '接收时间',
    `processed_time`  TIMESTAMP             DEFAULT NULL COMMENT '处理完成时间',
    `description`     VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`       BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`       BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`     INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`       TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wms_inbox_event` (`consumer_name`, `topic`, `event_id`),
    KEY `idx_wms_inbox_business_key` (`business_key`),
    KEY `idx_wms_inbox_process_state` (`process_state`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='WMS 消费消息接收与幂等记录';

-- ---------- 7. Outbox ----------
CREATE TABLE IF NOT EXISTS `tb_wms_outbox`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_id`          VARCHAR(64)  NOT NULL COMMENT '事件唯一 ID',
    `event_type`        VARCHAR(128) NOT NULL COMMENT '事件类型',
    `aggregate_type`    VARCHAR(64)  NOT NULL COMMENT '聚合类型',
    `aggregate_id`      VARCHAR(255) NOT NULL COMMENT '聚合 ID',
    `aggregate_version` BIGINT       NOT NULL COMMENT '聚合版本',
    `topic`             VARCHAR(255) NOT NULL COMMENT '注册的目标 Topic',
    `message_key`       VARCHAR(255) NOT NULL COMMENT 'Kafka Key',
    `payload`           TEXT         NOT NULL COMMENT '不可变 JSON 载荷',
    `publish_state`     VARCHAR(32)  NOT NULL COMMENT '发布状态：PENDING/FAILED_RETRYABLE/PUBLISHED/DEAD',
    `retry_count`       INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `next_retry_time`   TIMESTAMP             DEFAULT NULL COMMENT '下次重试时间',
    `published_at`      TIMESTAMP             DEFAULT NULL COMMENT 'Broker 确认时间',
    `last_error`        VARCHAR(2000)         DEFAULT NULL COMMENT '脱敏的最后错误',
    `description`       VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wms_outbox_event` (`event_id`),
    KEY `idx_wms_outbox_publish_retry` (`publish_state`, `next_retry_time`),
    KEY `idx_wms_outbox_aggregate` (`aggregate_id`, `aggregate_version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='WMS 事务性事件发布记录';
