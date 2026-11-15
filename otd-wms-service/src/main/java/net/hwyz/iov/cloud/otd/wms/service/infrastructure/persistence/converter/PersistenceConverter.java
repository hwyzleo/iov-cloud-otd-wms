package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboxProcessState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboxPublishState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageAreaInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageLocationInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.WarehouseInfo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InboundOrderPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryTransferPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.PreInboundOrderPo;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WmsInboxPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WmsOutboxPo;

import java.util.ArrayList;
import java.util.List;

/**
 * 持久化对象与领域模型显式转换器
 * <p>
 * 归属基础设施层：DO ⇄ Domain 转换集中在此，禁止跨层隐式传递。
 *
 * @author hwyz_leo
 */
public final class PersistenceConverter {

    private PersistenceConverter() {
    }

    // ---------------- 预入库单 ----------------

    public static PreInboundOrder toDomain(PreInboundOrderPo po) {
        if (po == null) {
            return null;
        }
        return PreInboundOrder.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .warehouseName(po.getWarehouseName())
                .warehouseLevel(po.getWarehouseLevel())
                .state(enumOf(PreInboundState.class, po.getState()))
                .sourceSystem(po.getSourceSystem())
                .sourceEventId(po.getSourceEventId())
                .sourceBatchNum(po.getSourceBatchNum())
                .estimatedArrivalTime(po.getEstimatedArrivalTime())
                .estimatedInboundTime(po.getEstimatedInboundTime())
                .arrivalTime(po.getArrivalTime())
                .inboundTime(po.getInboundTime())
                .inboundBy(toOperator(po.getInboundBy()))
                .rowVersion(po.getRowVersion())
                .createTime(po.getCreateTime())
                .build();
    }

    public static PreInboundOrderPo toPo(PreInboundOrder order) {
        if (order == null) {
            return null;
        }
        PreInboundOrderPo po = PreInboundOrderPo.builder()
                .id(order.getId())
                .orderNum(order.getOrderNum())
                .vin(order.getVin())
                .buildConfigCode(order.getBuildConfigCode())
                .warehouseCode(order.getWarehouseCode())
                .state(order.getState() == null ? null : order.getState().name())
                .sourceSystem(order.getSourceSystem())
                .sourceEventId(order.getSourceEventId())
                .sourceBatchNum(order.getSourceBatchNum())
                .estimatedArrivalTime(order.getEstimatedArrivalTime())
                .estimatedInboundTime(order.getEstimatedInboundTime())
                .audit(Boolean.FALSE)
                .arrival(order.getArrivalTime() != null)
                .arrivalTime(order.getArrivalTime())
                .inbound(order.getState() == PreInboundState.COMPLETED)
                .inboundTime(order.getInboundTime())
                .inboundBy(toOperatorId(order.getInboundBy()))
                .build();
        po.setRowVersion(order.getRowVersion());
        applyInsertDefaults(po);
        return po;
    }

    // ---------------- 库存 ----------------

    public static VehicleInventory toDomain(InventoryPo po) {
        if (po == null) {
            return null;
        }
        return VehicleInventory.builder()
                .id(po.getId())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .warehouseName(po.getWarehouseName())
                .warehouseLevel(po.getWarehouseLevel())
                .storageAreaCode(po.getStorageAreaCode())
                .storageLocationCode(po.getStorageLocationCode())
                .state(InventoryState.valOf(po.getState()))
                .reservedOrderNum(po.getReservedOrderNum())
                .inboundTime(po.getInboundTime())
                .inboundBy(toOperator(po.getInboundBy()))
                .outboundTime(po.getOutboundTime())
                .outboundBy(toOperator(po.getOutboundBy()))
                .rowVersion(po.getRowVersion())
                .createTime(po.getCreateTime())
                .build();
    }

    public static InventoryPo toPo(VehicleInventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryPo po = InventoryPo.builder()
                .id(inventory.getId())
                .vin(inventory.getVin())
                .buildConfigCode(inventory.getBuildConfigCode())
                .warehouseCode(inventory.getWarehouseCode())
                .storageAreaCode(inventory.getStorageAreaCode())
                .storageLocationCode(inventory.getStorageLocationCode())
                .state(inventory.getState() == null ? null : inventory.getState().value())
                .reservedOrderNum(inventory.getReservedOrderNum())
                .inboundTime(inventory.getInboundTime())
                .inboundBy(toOperatorId(inventory.getInboundBy()))
                .outboundTime(inventory.getOutboundTime())
                .outboundBy(toOperatorId(inventory.getOutboundBy()))
                .build();
        po.setRowVersion(inventory.getRowVersion());
        applyInsertDefaults(po);
        return po;
    }

    // ---------------- 入库单 ----------------

    public static InboundOrderFact toDomain(InboundOrderPo po) {
        if (po == null) {
            return null;
        }
        return InboundOrderFact.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .preOrderNum(po.getPreOrderNum())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .warehouseName(po.getWarehouseName())
                .warehouseLevel(po.getWarehouseLevel())
                .storageAreaCode(po.getStorageAreaCode())
                .storageLocationCode(po.getStorageLocationCode())
                .inboundTime(po.getInboundTime())
                .inboundBy(toOperator(po.getInboundBy()))
                .createTime(po.getCreateTime())
                .build();
    }

    public static InboundOrderPo toPo(InboundOrderFact fact) {
        if (fact == null) {
            return null;
        }
        InboundOrderPo factPo = InboundOrderPo.builder()
                .id(fact.getId())
                .orderNum(fact.getOrderNum())
                .preOrderNum(fact.getPreOrderNum())
                .vin(fact.getVin())
                .buildConfigCode(fact.getBuildConfigCode())
                .warehouseCode(fact.getWarehouseCode())
                .storageAreaCode(fact.getStorageAreaCode())
                .storageLocationCode(fact.getStorageLocationCode())
                .inboundTime(fact.getInboundTime())
                .inboundBy(toOperatorId(fact.getInboundBy()))
                .build();
        applyInsertDefaults(factPo);
        return factPo;
    }

    // ---------------- 移储 ----------------

    public static InventoryTransferRecord toDomain(InventoryTransferPo po) {
        if (po == null) {
            return null;
        }
        return InventoryTransferRecord.builder()
                .id(po.getId())
                .requestId(po.getRequestId())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .warehouseName(po.getWarehouseName())
                .warehouseLevel(po.getWarehouseLevel())
                .oldStorageAreaCode(po.getOldStorageAreaCode())
                .oldStorageLocationCode(po.getOldStorageLocationCode())
                .newStorageAreaCode(po.getNewStorageAreaCode())
                .newStorageLocationCode(po.getNewStorageLocationCode())
                .transferTime(po.getTransferTime())
                .transferBy(toOperator(po.getTransferBy()))
                .createTime(po.getCreateTime())
                .build();
    }

    public static InventoryTransferPo toPo(InventoryTransferRecord record) {
        if (record == null) {
            return null;
        }
        InventoryTransferPo transferPo = InventoryTransferPo.builder()
                .id(record.getId())
                .requestId(record.getRequestId())
                .vin(record.getVin())
                .buildConfigCode(record.getBuildConfigCode())
                .warehouseCode(record.getWarehouseCode())
                .oldStorageAreaCode(record.getOldStorageAreaCode())
                .oldStorageLocationCode(record.getOldStorageLocationCode())
                .newStorageAreaCode(record.getNewStorageAreaCode())
                .newStorageLocationCode(record.getNewStorageLocationCode())
                .transferState((short) 1)
                .transferTime(record.getTransferTime())
                .transferBy(toOperatorId(record.getTransferBy()))
                .build();
        applyInsertDefaults(transferPo);
        return transferPo;
    }

    // ---------------- 出库单 ----------------

    public static OutboundOrder toDomain(OutboundOrderPo po, List<OutboundOrderDetailPo> detailPoList) {
        if (po == null) {
            return null;
        }
        List<OutboundOrderDetail> details = new ArrayList<>();
        if (detailPoList != null) {
            for (OutboundOrderDetailPo detailPo : detailPoList) {
                details.add(toDomain(detailPo));
            }
        }
        return OutboundOrder.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .requestId(po.getRequestId())
                .sourceSystem(po.getSourceSystem())
                .sourceOrderNum(po.getSourceOrderNum())
                .warehouseCode(po.getWarehouseCode())
                .state(enumOf(OutboundOrderState.class, po.getState()))
                .aggregateVersion(po.getAggregateVersion())
                .rowVersion(po.getRowVersion())
                .createTime(po.getCreateTime())
                .details(details)
                .build();
    }

    public static OutboundOrderPo toPo(OutboundOrder order) {
        if (order == null) {
            return null;
        }
        OutboundOrderPo po = OutboundOrderPo.builder()
                .id(order.getId())
                .orderNum(order.getOrderNum())
                .requestId(order.getRequestId())
                .sourceSystem(order.getSourceSystem())
                .sourceOrderNum(order.getSourceOrderNum())
                .warehouseCode(order.getWarehouseCode())
                .state(order.getState() == null ? null : order.getState().name())
                .aggregateVersion(order.getAggregateVersion())
                .build();
        po.setRowVersion(order.getRowVersion());
        applyInsertDefaults(po);
        return po;
    }

    public static OutboundOrderDetail toDomain(OutboundOrderDetailPo po) {
        if (po == null) {
            return null;
        }
        return OutboundOrderDetail.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .storageAreaCode(po.getStorageAreaCode())
                .storageLocationCode(po.getStorageLocationCode())
                .state(enumOf(OutboundDetailState.class, po.getState()))
                .reserveTime(po.getReserveTime())
                .reserveBy(toOperator(po.getReserveBy()))
                .loadTime(po.getLoadTime())
                .loadBy(toOperator(po.getLoadBy()))
                .outboundTime(po.getOutboundTime())
                .outboundBy(toOperator(po.getOutboundBy()))
                .releaseTime(po.getReleaseTime())
                .releaseBy(toOperator(po.getReleaseBy()))
                .rowVersion(po.getRowVersion())
                .build();
    }

    public static OutboundOrderDetailPo toPo(OutboundOrderDetail detail) {
        if (detail == null) {
            return null;
        }
        OutboundOrderDetailPo po = OutboundOrderDetailPo.builder()
                .id(detail.getId())
                .orderNum(detail.getOrderNum())
                .vin(detail.getVin())
                .buildConfigCode(detail.getBuildConfigCode())
                .warehouseCode(detail.getWarehouseCode())
                .storageAreaCode(detail.getStorageAreaCode())
                .storageLocationCode(detail.getStorageLocationCode())
                .state(detail.getState() == null ? null : detail.getState().name())
                .reserveTime(detail.getReserveTime())
                .reserveBy(toOperatorId(detail.getReserveBy()))
                .loadTime(detail.getLoadTime())
                .loadBy(toOperatorId(detail.getLoadBy()))
                .outboundTime(detail.getOutboundTime())
                .outboundBy(toOperatorId(detail.getOutboundBy()))
                .releaseTime(detail.getReleaseTime())
                .releaseBy(toOperatorId(detail.getReleaseBy()))
                .build();
        po.setRowVersion(detail.getRowVersion());
        applyInsertDefaults(po);
        return po;
    }

    // ---------------- Inbox / Outbox ----------------

    public static InboxRecord toDomain(WmsInboxPo po) {
        if (po == null) {
            return null;
        }
        return InboxRecord.builder()
                .consumerName(po.getConsumerName())
                .topic(po.getTopic())
                .eventId(po.getEventId())
                .eventType(po.getEventType())
                .payloadDigest(po.getPayloadDigest())
                .processState(enumOf(InboxProcessState.class, po.getProcessState()))
                .businessKey(po.getBusinessKey())
                .resultEventId(po.getResultEventId())
                .errorCode(po.getErrorCode())
                .errorMessage(po.getErrorMessage())
                .receivedTime(po.getReceivedTime())
                .processedTime(po.getProcessedTime())
                .build();
    }

    public static WmsInboxPo toPo(InboxRecord record) {
        if (record == null) {
            return null;
        }
        WmsInboxPo inboxPo = WmsInboxPo.builder()
                .consumerName(record.getConsumerName())
                .topic(record.getTopic())
                .eventId(record.getEventId())
                .eventType(record.getEventType())
                .payloadDigest(record.getPayloadDigest())
                .processState(record.getProcessState() == null ? null : record.getProcessState().name())
                .businessKey(record.getBusinessKey())
                .resultEventId(record.getResultEventId())
                .errorCode(record.getErrorCode())
                .errorMessage(record.getErrorMessage())
                .receivedTime(record.getReceivedTime())
                .processedTime(record.getProcessedTime())
                .build();
        applyInsertDefaults(inboxPo);
        return inboxPo;
    }

    public static OutboxRecord toDomain(WmsOutboxPo po) {
        if (po == null) {
            return null;
        }
        return OutboxRecord.builder()
                .id(po.getId())
                .eventId(po.getEventId())
                .eventType(po.getEventType())
                .aggregateType(po.getAggregateType())
                .aggregateId(po.getAggregateId())
                .aggregateVersion(po.getAggregateVersion())
                .topic(po.getTopic())
                .messageKey(po.getMessageKey())
                .payload(po.getPayload())
                .publishState(enumOf(OutboxPublishState.class, po.getPublishState()))
                .retryCount(po.getRetryCount())
                .nextRetryTime(po.getNextRetryTime())
                .publishedAt(po.getPublishedAt())
                .lastError(po.getLastError())
                .build();
    }

    public static WmsOutboxPo toPo(OutboxRecord record) {
        if (record == null) {
            return null;
        }
        WmsOutboxPo outboxPo = WmsOutboxPo.builder()
                .id(record.getId())
                .eventId(record.getEventId())
                .eventType(record.getEventType())
                .aggregateType(record.getAggregateType())
                .aggregateId(record.getAggregateId())
                .aggregateVersion(record.getAggregateVersion())
                .topic(record.getTopic())
                .messageKey(record.getMessageKey())
                .payload(record.getPayload())
                .publishState(record.getPublishState() == null ? null : record.getPublishState().name())
                .retryCount(record.getRetryCount() == null ? 0 : record.getRetryCount())
                .nextRetryTime(record.getNextRetryTime())
                .publishedAt(record.getPublishedAt())
                .lastError(record.getLastError())
                .build();
        applyInsertDefaults(outboxPo);
        return outboxPo;
    }

    // ---------------- 仓储主数据 ----------------

    public static WarehouseInfo toDomain(WarehousePo po) {
        if (po == null) {
            return null;
        }
        return new WarehouseInfo(po.getCode(), po.getName(), po.getWarehouseLevel(), Boolean.TRUE.equals(po.getEnable()));
    }

    public static StorageAreaInfo toDomain(StorageAreaPo po) {
        if (po == null) {
            return null;
        }
        return new StorageAreaInfo(po.getCode(), po.getName(), po.getWarehouseCode(), Boolean.TRUE.equals(po.getEnable()));
    }

    public static StorageLocationInfo toDomain(StorageLocationPo po) {
        if (po == null) {
            return null;
        }
        return new StorageLocationInfo(po.getCode(), po.getName(), po.getWarehouseCode(), po.getStorageAreaCode(),
                Boolean.TRUE.equals(po.getEnable()));
    }

    /**
     * 操作人标识（字符串）转数据库用户ID
     *
     * @param operator 操作人标识
     * @return 用户ID（非数值标识按系统用户 0 处理）
     */
    private static Long toOperatorId(String operator) {
        if (operator == null || operator.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(operator);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 数据库用户ID转操作人标识
     *
     * @param operatorId 用户ID
     * @return 操作人标识
     */
    private static String toOperator(Long operatorId) {
        return operatorId == null ? null : operatorId.toString();
    }

    /**
     * 插入默认值：记录版本与有效性（不依赖运行期元数据填充）
     *
     * @param po 数据对象
     */
    private static void applyInsertDefaults(BasePo po) {
        if (po.getRowVersion() == null) {
            po.setRowVersion(1);
        }
        if (po.getRowValid() == null) {
            po.setRowValid(Boolean.TRUE);
        }
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, name);
    }
}
