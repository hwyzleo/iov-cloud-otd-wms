-- =====================================================================
-- WMS-DSN-CR-001 US-001 仓储主数据编码在同一层级内唯一
--
-- 幂等性：MySQL DDL 非事务，索引创建使用 information_schema 守卫，可安全重跑。
-- 迁移门禁：存量重复编码会使唯一约束失败，需按 doc/sql/cr001_preflight.sql
-- 报告结果走已批准的清理方案，不得静默合并主数据。
-- =====================================================================

-- 储区编码在仓库内唯一
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_storage_area'
                  AND INDEX_NAME = 'uk_storage_area_warehouse_code') = 0,
               'ALTER TABLE `tb_storage_area` ADD UNIQUE KEY `uk_storage_area_warehouse_code` (`warehouse_code`, `code`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 储位编码在同一仓库同一储区内唯一
SET @ddl := IF((SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_storage_location'
                  AND INDEX_NAME = 'uk_storage_location_code') = 0,
               'ALTER TABLE `tb_storage_location` ADD UNIQUE KEY `uk_storage_location_code` (`warehouse_code`, `storage_area_code`, `code`)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
