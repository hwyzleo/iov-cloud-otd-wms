package net.hwyz.iov.cloud.otd.wms.service.application.assembler;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryTransferDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.OutboundOrderDetailDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.OutboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.PreInboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageAreaDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageLocationDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.WarehouseDto;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;

import java.util.List;

/**
 * 领域模型／主数据对象 → 应用查询 DTO 转换
 * <p>
 * 归属应用层：对上层只暴露 DTO，隔离领域模型与持久化对象。
 *
 * @author hwyz_leo
 */
public final class WmsResultAssembler {

    private WmsResultAssembler() {
    }

    public static PreInboundOrderDto fromPreInbound(PreInboundOrder order) {
        if (order == null) {
            return null;
        }
        return PreInboundOrderDto.builder()
                .id(order.getId())
                .orderNum(order.getOrderNum())
                .vin(order.getVin())
                .buildConfigCode(order.getBuildConfigCode())
                .warehouseCode(order.getWarehouseCode())
                .warehouseName(order.getWarehouseName())
                .warehouseLevel(order.getWarehouseLevel())
                .state(order.getState() == null ? null : order.getState().name())
                .sourceSystem(order.getSourceSystem())
                .sourceEventId(order.getSourceEventId())
                .sourceBatchNum(order.getSourceBatchNum())
                .estimatedArrivalTime(order.getEstimatedArrivalTime())
                .estimatedInboundTime(order.getEstimatedInboundTime())
                .arrivalTime(order.getArrivalTime())
                .inboundTime(order.getInboundTime())
                .rowVersion(order.getRowVersion())
                .createTime(order.getCreateTime())
                .build();
    }

    public static List<PreInboundOrderDto> fromPreInboundList(List<PreInboundOrder> list) {
        return list.stream().map(WmsResultAssembler::fromPreInbound).toList();
    }

    public static InboundOrderDto fromInbound(InboundOrderFact fact) {
        if (fact == null) {
            return null;
        }
        return InboundOrderDto.builder()
                .id(fact.getId())
                .orderNum(fact.getOrderNum())
                .preOrderNum(fact.getPreOrderNum())
                .vin(fact.getVin())
                .buildConfigCode(fact.getBuildConfigCode())
                .warehouseCode(fact.getWarehouseCode())
                .warehouseName(fact.getWarehouseName())
                .warehouseLevel(fact.getWarehouseLevel())
                .storageAreaCode(fact.getStorageAreaCode())
                .storageLocationCode(fact.getStorageLocationCode())
                .inboundTime(fact.getInboundTime())
                .inboundBy(fact.getInboundBy())
                .createTime(fact.getCreateTime())
                .build();
    }

    public static List<InboundOrderDto> fromInboundList(List<InboundOrderFact> list) {
        return list.stream().map(WmsResultAssembler::fromInbound).toList();
    }

    public static InventoryDto fromInventory(VehicleInventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryDto.builder()
                .id(inventory.getId())
                .vin(inventory.getVin())
                .buildConfigCode(inventory.getBuildConfigCode())
                .warehouseCode(inventory.getWarehouseCode())
                .warehouseName(inventory.getWarehouseName())
                .warehouseLevel(inventory.getWarehouseLevel())
                .storageAreaCode(inventory.getStorageAreaCode())
                .storageLocationCode(inventory.getStorageLocationCode())
                .state(inventory.getState() == null ? null : inventory.getState().value())
                .stateName(inventory.getState() == null ? null : inventory.getState().name())
                .reservedOrderNum(inventory.getReservedOrderNum())
                .inboundTime(inventory.getInboundTime())
                .inboundBy(inventory.getInboundBy())
                .outboundTime(inventory.getOutboundTime())
                .outboundBy(inventory.getOutboundBy())
                .rowVersion(inventory.getRowVersion())
                .createTime(inventory.getCreateTime())
                .build();
    }

    public static List<InventoryDto> fromInventoryList(List<VehicleInventory> list) {
        return list.stream().map(WmsResultAssembler::fromInventory).toList();
    }

    public static InventoryTransferDto fromTransfer(InventoryTransferRecord record) {
        if (record == null) {
            return null;
        }
        return InventoryTransferDto.builder()
                .id(record.getId())
                .requestId(record.getRequestId())
                .vin(record.getVin())
                .buildConfigCode(record.getBuildConfigCode())
                .warehouseCode(record.getWarehouseCode())
                .warehouseName(record.getWarehouseName())
                .warehouseLevel(record.getWarehouseLevel())
                .oldStorageAreaCode(record.getOldStorageAreaCode())
                .oldStorageLocationCode(record.getOldStorageLocationCode())
                .newStorageAreaCode(record.getNewStorageAreaCode())
                .newStorageLocationCode(record.getNewStorageLocationCode())
                .transferTime(record.getTransferTime())
                .transferBy(record.getTransferBy())
                .createTime(record.getCreateTime())
                .build();
    }

    public static List<InventoryTransferDto> fromTransferList(List<InventoryTransferRecord> list) {
        return list.stream().map(WmsResultAssembler::fromTransfer).toList();
    }

    public static OutboundOrderDto fromOutbound(OutboundOrder order) {
        if (order == null) {
            return null;
        }
        return OutboundOrderDto.builder()
                .id(order.getId())
                .orderNum(order.getOrderNum())
                .requestId(order.getRequestId())
                .sourceSystem(order.getSourceSystem())
                .sourceOrderNum(order.getSourceOrderNum())
                .warehouseCode(order.getWarehouseCode())
                .state(order.getState() == null ? null : order.getState().name())
                .aggregateVersion(order.getAggregateVersion())
                .rowVersion(order.getRowVersion())
                .createTime(order.getCreateTime())
                .build();
    }

    public static List<OutboundOrderDto> fromOutboundList(List<OutboundOrder> list) {
        return list.stream().map(WmsResultAssembler::fromOutbound).toList();
    }

    public static OutboundOrderDetailDto fromOutboundDetail(OutboundOrderDetail detail) {
        if (detail == null) {
            return null;
        }
        return OutboundOrderDetailDto.builder()
                .id(detail.getId())
                .orderNum(detail.getOrderNum())
                .vin(detail.getVin())
                .buildConfigCode(detail.getBuildConfigCode())
                .warehouseCode(detail.getWarehouseCode())
                .storageAreaCode(detail.getStorageAreaCode())
                .storageLocationCode(detail.getStorageLocationCode())
                .state(detail.getState() == null ? null : detail.getState().name())
                .reserveTime(detail.getReserveTime())
                .loadTime(detail.getLoadTime())
                .outboundTime(detail.getOutboundTime())
                .releaseTime(detail.getReleaseTime())
                .rowVersion(detail.getRowVersion())
                .build();
    }

    public static List<OutboundOrderDetailDto> fromOutboundDetailList(List<OutboundOrderDetail> list) {
        return list.stream().map(WmsResultAssembler::fromOutboundDetail).toList();
    }

    public static WarehouseDto fromWarehouse(WarehousePo po) {
        if (po == null) {
            return null;
        }
        return WarehouseDto.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .manager(po.getManager())
                .warehouseLevel(po.getWarehouseLevel())
                .provinceCode(po.getProvinceCode())
                .cityCode(po.getCityCode())
                .countyCode(po.getCountyCode())
                .address(po.getAddress())
                .zipcode(po.getZipcode())
                .fax(po.getFax())
                .tel(po.getTel())
                .mobile(po.getMobile())
                .enable(po.getEnable())
                .sort(po.getSort())
                .createTime(po.getCreateTime())
                .build();
    }

    public static List<WarehouseDto> fromWarehouseList(List<WarehousePo> list) {
        return list.stream().map(WmsResultAssembler::fromWarehouse).toList();
    }

    public static StorageAreaDto fromStorageArea(StorageAreaPo po) {
        if (po == null) {
            return null;
        }
        return StorageAreaDto.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .warehouseCode(po.getWarehouseCode())
                .manager(po.getManager())
                .enable(po.getEnable())
                .sort(po.getSort())
                .createTime(po.getCreateTime())
                .build();
    }

    public static List<StorageAreaDto> fromStorageAreaList(List<StorageAreaPo> list) {
        return list.stream().map(WmsResultAssembler::fromStorageArea).toList();
    }

    public static StorageLocationDto fromStorageLocation(StorageLocationPo po) {
        if (po == null) {
            return null;
        }
        return StorageLocationDto.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .warehouseCode(po.getWarehouseCode())
                .storageAreaCode(po.getStorageAreaCode())
                .manager(po.getManager())
                .enable(po.getEnable())
                .sort(po.getSort())
                .createTime(po.getCreateTime())
                .build();
    }

    public static List<StorageLocationDto> fromStorageLocationList(List<StorageLocationPo> list) {
        return list.stream().map(WmsResultAssembler::fromStorageLocation).toList();
    }
}
