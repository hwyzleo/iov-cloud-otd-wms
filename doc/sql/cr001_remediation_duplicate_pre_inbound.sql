-- =====================================================================
-- WMS-DSN-CR-001 存量重复预入库 VIN 清理（已批准清理方案）
--
-- 背景：基线同步接口 POST /service/preInboundOrder 无去重，反复调用会在
--       tb_pre_inbound_order 产生同一 VIN 的多条 PENDING_ARRIVAL 记录，
--       使 V2 的 uk_pre_inbound_vin 无法建立。
-- 方案：同一 VIN 仅保留最新一条（id 最大），删除其余重复记录。
--       不合并车辆、不改动其他 VIN、不改动库存与已完成事实。
-- 前置：执行前先运行 doc/sql/cr001_preflight.sql 确认重复范围与预期一致。
-- 执行后：删除失败的 Flyway 记录（等价 flyway repair），再重启应用完成 V2/V3。
-- =====================================================================

-- 1. 待删除明细（执行 DELETE 前先核对）
SELECT id, order_num, vin, warehouse_code, state, arrival, inbound, create_time
FROM tb_pre_inbound_order t
WHERE t.vin IN (SELECT vin FROM (SELECT vin FROM tb_pre_inbound_order GROUP BY vin HAVING COUNT(*) > 1) d)
  AND t.id <> (SELECT keep_id FROM (SELECT MAX(id) AS keep_id FROM tb_pre_inbound_order p
                                    WHERE p.vin = t.vin) k)
ORDER BY t.vin, t.id;

-- 2. 执行清理（按同一规则：每 VIN 保留最新一条）
DELETE FROM tb_pre_inbound_order
WHERE id IN (SELECT id FROM (SELECT t.id
                             FROM tb_pre_inbound_order t
                             WHERE t.vin IN (SELECT vin
                                             FROM (SELECT vin FROM tb_pre_inbound_order
                                                   GROUP BY vin HAVING COUNT(*) > 1) d)
                               AND t.id <> (SELECT keep_id
                                            FROM (SELECT MAX(id) AS keep_id
                                                  FROM tb_pre_inbound_order p
                                                  WHERE p.vin = t.vin) k)) x);

-- 3. 清理失败的 Flyway 迁移记录（等价 flyway repair；仅删除失败记录）
DELETE FROM flyway_schema_history WHERE success = 0;

-- 4. 复核：重复项应为 0 行
SELECT vin, COUNT(*) AS c FROM tb_pre_inbound_order GROUP BY vin HAVING COUNT(*) > 1;
