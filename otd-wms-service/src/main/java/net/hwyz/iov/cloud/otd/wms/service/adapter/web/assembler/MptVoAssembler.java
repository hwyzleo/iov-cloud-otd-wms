package net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageAreaMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageLocationMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryTransferDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InboundTaskDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.OutboundOrderDetailDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.OutboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.PreInboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageAreaDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageLocationDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.WarehouseDto;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;

import java.util.List;

/**
 * 应用 DTO → 管理后台 VO 转换（接入层）
 *
 * @author hwyz_leo
 */
public final class MptVoAssembler {

    private MptVoAssembler() {
    }

    public static PreInboundOrderMpt fromPreInbound(PreInboundOrderDto dto) {
        if (dto == null) {
            return null;
        }
        boolean arrived = PreInboundState.ARRIVED.name().equals(dto.state())
                || PreInboundState.COMPLETED.name().equals(dto.state());
        boolean inbound = PreInboundState.COMPLETED.name().equals(dto.state());
        return PreInboundOrderMpt.builder()
                .id(dto.id())
                .orderNum(dto.orderNum())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseLevel(dto.warehouseLevel())
                .warehouseCode(dto.warehouseCode())
                .warehouseName(dto.warehouseName())
                .state(dto.state())
                .sourceSystem(dto.sourceSystem())
                .sourceEventId(dto.sourceEventId())
                .sourceBatchNum(dto.sourceBatchNum())
                .rowVersion(dto.rowVersion())
                .estimatedArrivalTime(dto.estimatedArrivalTime())
                .estimatedInboundTime(dto.estimatedInboundTime())
                .arrival(arrived)
                .arrivalTime(dto.arrivalTime())
                .inbound(inbound)
                .inboundTime(dto.inboundTime())
                .createTime(dto.createTime())
                .build();
    }

    public static List<PreInboundOrderMpt> fromPreInboundList(List<PreInboundOrderDto> list) {
        return list.stream().map(MptVoAssembler::fromPreInbound).toList();
    }

    public static InboundOrderMpt fromInbound(InboundOrderDto dto) {
        if (dto == null) {
            return null;
        }
        return InboundOrderMpt.builder()
                .id(dto.id())
                .orderNum(dto.orderNum())
                .preOrderNum(dto.preOrderNum())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseLevel(dto.warehouseLevel())
                .warehouseCode(dto.warehouseCode())
                .warehouseName(dto.warehouseName())
                .storageAreaCode(dto.storageAreaCode())
                .storageLocationCode(dto.storageLocationCode())
                .inboundTime(dto.inboundTime())
                .inboundBy(operatorId(dto.inboundBy()))
                .createTime(dto.createTime())
                .build();
    }

    public static List<InboundOrderMpt> fromInboundList(List<InboundOrderDto> list) {
        return list.stream().map(MptVoAssembler::fromInbound).toList();
    }

    public static InventoryMpt fromInventory(InventoryDto dto) {
        if (dto == null) {
            return null;
        }
        return InventoryMpt.builder()
                .id(dto.id())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseCode(dto.warehouseCode())
                .warehouseLevel(dto.warehouseLevel())
                .warehouseName(dto.warehouseName())
                .storageAreaCode(dto.storageAreaCode())
                .storageLocationCode(dto.storageLocationCode())
                .state(dto.state())
                .stateName(dto.stateName())
                .reservedOrderNum(dto.reservedOrderNum())
                .rowVersion(dto.rowVersion())
                .inboundTime(dto.inboundTime())
                .outboundTime(dto.outboundTime())
                .createTime(dto.createTime())
                .build();
    }

    public static List<InventoryMpt> fromInventoryList(List<InventoryDto> list) {
        return list.stream().map(MptVoAssembler::fromInventory).toList();
    }

    public static InventoryTransferMpt fromTransfer(InventoryTransferDto dto) {
        if (dto == null) {
            return null;
        }
        return InventoryTransferMpt.builder()
                .id(dto.id())
                .requestId(dto.requestId())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseCode(dto.warehouseCode())
                .warehouseName(dto.warehouseName())
                .warehouseLevel(dto.warehouseLevel())
                .oldStorageAreaCode(dto.oldStorageAreaCode())
                .oldStorageLocationCode(dto.oldStorageLocationCode())
                .newStorageAreaCode(dto.newStorageAreaCode())
                .newStorageLocationCode(dto.newStorageLocationCode())
                .transferTime(dto.transferTime())
                .transferBy(operatorId(dto.transferBy()))
                .createTime(dto.createTime())
                .build();
    }

    public static List<InventoryTransferMpt> fromTransferList(List<InventoryTransferDto> list) {
        return list.stream().map(MptVoAssembler::fromTransfer).toList();
    }

    public static OutboundOrderMpt fromOutbound(OutboundOrderDto dto) {
        if (dto == null) {
            return null;
        }
        return OutboundOrderMpt.builder()
                .id(dto.id())
                .orderNum(dto.orderNum())
                .requestId(dto.requestId())
                .sourceSystem(dto.sourceSystem())
                .sourceOrderNum(dto.sourceOrderNum())
                .warehouseCode(dto.warehouseCode())
                .state(dto.state())
                .aggregateVersion(dto.aggregateVersion())
                .rowVersion(dto.rowVersion())
                .createTime(dto.createTime())
                .build();
    }

    public static List<OutboundOrderMpt> fromOutboundList(List<OutboundOrderDto> list) {
        return list.stream().map(MptVoAssembler::fromOutbound).toList();
    }

    public static OutboundOrderDetailMpt fromOutboundDetail(OutboundOrderDetailDto dto) {
        if (dto == null) {
            return null;
        }
        return OutboundOrderDetailMpt.builder()
                .id(dto.id())
                .orderNum(dto.orderNum())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseCode(dto.warehouseCode())
                .storageAreaCode(dto.storageAreaCode())
                .storageLocationCode(dto.storageLocationCode())
                .state(dto.state())
                .reserveTime(dto.reserveTime())
                .loadTime(dto.loadTime())
                .outboundTime(dto.outboundTime())
                .releaseTime(dto.releaseTime())
                .rowVersion(dto.rowVersion())
                .build();
    }

    public static List<OutboundOrderDetailMpt> fromOutboundDetailList(List<OutboundOrderDetailDto> list) {
        return list.stream().map(MptVoAssembler::fromOutboundDetail).toList();
    }

    public static WarehouseMpt fromWarehouse(WarehouseDto dto) {
        if (dto == null) {
            return null;
        }
        return WarehouseMpt.builder()
                .id(dto.id())
                .code(dto.code())
                .name(dto.name())
                .manager(dto.manager())
                .warehouseLevel(dto.warehouseLevel())
                .provinceCode(dto.provinceCode())
                .cityCode(dto.cityCode())
                .countyCode(dto.countyCode())
                .address(dto.address())
                .zipcode(dto.zipcode())
                .fax(dto.fax())
                .tel(dto.tel())
                .mobile(dto.mobile())
                .enable(dto.enable())
                .sort(dto.sort())
                .createTime(dto.createTime())
                .build();
    }

    public static List<WarehouseMpt> fromWarehouseList(List<WarehouseDto> list) {
        return list.stream().map(MptVoAssembler::fromWarehouse).toList();
    }

    public static StorageAreaMpt fromStorageArea(StorageAreaDto dto) {
        if (dto == null) {
            return null;
        }
        return StorageAreaMpt.builder()
                .id(dto.id())
                .code(dto.code())
                .name(dto.name())
                .warehouseCode(dto.warehouseCode())
                .manager(dto.manager())
                .enable(dto.enable())
                .sort(dto.sort())
                .createTime(dto.createTime())
                .build();
    }

    public static List<StorageAreaMpt> fromStorageAreaList(List<StorageAreaDto> list) {
        return list.stream().map(MptVoAssembler::fromStorageArea).toList();
    }

    public static StorageLocationMpt fromStorageLocation(StorageLocationDto dto) {
        if (dto == null) {
            return null;
        }
        return StorageLocationMpt.builder()
                .id(dto.id())
                .code(dto.code())
                .name(dto.name())
                .warehouseCode(dto.warehouseCode())
                .storageAreaCode(dto.storageAreaCode())
                .manager(dto.manager())
                .enable(dto.enable())
                .sort(dto.sort())
                .createTime(dto.createTime())
                .build();
    }

    public static List<StorageLocationMpt> fromStorageLocationList(List<StorageLocationDto> list) {
        return list.stream().map(MptVoAssembler::fromStorageLocation).toList();
    }

    // ---------------- 盘点（未纳管模块，保持既有 PO → VO 转换） ----------------

    public static InventoryCountMpt fromInventoryCount(InventoryCountPo po) {
        if (po == null) {
            return null;
        }
        return InventoryCountMpt.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .type(po.getType())
                .warehouseCode(po.getWarehouseCode())
                .warehouseName(po.getWarehouseName())
                .warehouseLevel(po.getWarehouseLevel())
                .storageAreaCode(po.getStorageAreaCode())
                .state(po.getState())
                .startTime(po.getStartTime())
                .endTime(po.getEndTime())
                .createTime(po.getCreateTime())
                .build();
    }

    public static List<InventoryCountMpt> fromInventoryCountList(List<InventoryCountPo> list) {
        return list.stream().map(MptVoAssembler::fromInventoryCount).toList();
    }

    public static InventoryCountDetailMpt fromInventoryCountDetail(InventoryCountDetailPo po) {
        if (po == null) {
            return null;
        }
        return InventoryCountDetailMpt.builder()
                .id(po.getId())
                .orderNum(po.getOrderNum())
                .vin(po.getVin())
                .buildConfigCode(po.getBuildConfigCode())
                .warehouseCode(po.getWarehouseCode())
                .storageAreaCode(po.getStorageAreaCode())
                .storageLocationCode(po.getStorageLocationCode())
                .actualWarehouseCode(po.getActualWarehouseCode())
                .actualStorageAreaCode(po.getActualStorageAreaCode())
                .actualStorageLocationCode(po.getActualStorageLocationCode())
                .countTime(po.getCountTime())
                .createTime(po.getCreateTime())
                .build();
    }

    public static List<InventoryCountDetailMpt> fromInventoryCountDetailList(List<InventoryCountDetailPo> list) {
        return list.stream().map(MptVoAssembler::fromInventoryCountDetail).toList();
    }

    // ---------------- 主数据请求 VO → PO ----------------

    public static WarehousePo toPo(WarehouseMpt vo) {
        return WarehousePo.builder()
                .id(vo.getId())
                .code(vo.getCode())
                .name(vo.getName())
                .manager(vo.getManager())
                .warehouseLevel(vo.getWarehouseLevel())
                .provinceCode(vo.getProvinceCode())
                .cityCode(vo.getCityCode())
                .countyCode(vo.getCountyCode())
                .address(vo.getAddress())
                .zipcode(vo.getZipcode())
                .fax(vo.getFax())
                .tel(vo.getTel())
                .mobile(vo.getMobile())
                .enable(vo.getEnable())
                .sort(vo.getSort())
                .build();
    }

    public static StorageAreaPo toPo(StorageAreaMpt vo) {
        return StorageAreaPo.builder()
                .id(vo.getId())
                .code(vo.getCode())
                .name(vo.getName())
                .warehouseCode(vo.getWarehouseCode())
                .manager(vo.getManager())
                .enable(vo.getEnable())
                .sort(vo.getSort())
                .build();
    }

    public static StorageLocationPo toPo(StorageLocationMpt vo) {
        return StorageLocationPo.builder()
                .id(vo.getId())
                .code(vo.getCode())
                .name(vo.getName())
                .warehouseCode(vo.getWarehouseCode())
                .storageAreaCode(vo.getStorageAreaCode())
                .manager(vo.getManager())
                .enable(vo.getEnable())
                .sort(vo.getSort())
                .build();
    }

    public static InventoryCountPo toPo(InventoryCountMpt vo) {
        return InventoryCountPo.builder()
                .id(vo.getId())
                .orderNum(vo.getOrderNum())
                .type(vo.getType())
                .warehouseCode(vo.getWarehouseCode())
                .storageAreaCode(vo.getStorageAreaCode())
                .state(vo.getState())
                .startTime(vo.getStartTime())
                .endTime(vo.getEndTime())
                .build();
    }

    /**
     * 操作人标识转换：领域／DTO 使用字符串，MPT VO 沿用 Long。
     * <p>
     * 非数字标识（异常场景）按空处理，不影响查询主流程。
     *
     * @param operator 操作人标识
     * @return Long 操作人
     */
    private static Long operatorId(String operator) {
        if (operator == null || operator.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(operator);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static InventoryCountDetailPo toPo(InventoryCountDetailMpt vo) {
        return InventoryCountDetailPo.builder()
                .id(vo.getId())
                .orderNum(vo.getOrderNum())
                .vin(vo.getVin())
                .buildConfigCode(vo.getBuildConfigCode())
                .warehouseCode(vo.getWarehouseCode())
                .storageAreaCode(vo.getStorageAreaCode())
                .storageLocationCode(vo.getStorageLocationCode())
                .actualWarehouseCode(vo.getActualWarehouseCode())
                .actualStorageAreaCode(vo.getActualStorageAreaCode())
                .actualStorageLocationCode(vo.getActualStorageLocationCode())
                .countTime(vo.getCountTime())
                .build();
    }

    public static InboundTaskMpt fromInboundTask(InboundTaskDto dto) {
        if (dto == null) {
            return null;
        }
        return InboundTaskMpt.builder()
                .id(dto.id())
                .orderNum(dto.orderNum())
                .requestId(dto.requestId())
                .sourceSystem(dto.sourceSystem())
                .sourceOrderNum(dto.sourceOrderNum())
                .vin(dto.vin())
                .buildConfigCode(dto.buildConfigCode())
                .warehouseCode(dto.warehouseCode())
                .state(dto.state())
                .estimatedArrivalTime(dto.estimatedArrivalTime())
                .arrivalTime(dto.arrivalTime())
                .inboundTime(dto.inboundTime())
                .cancelTime(dto.cancelTime())
                .cancelReason(dto.cancelReason())
                .lifecycleSeq(dto.lifecycleSeq())
                .aggregateVersion(dto.aggregateVersion())
                .rowVersion(dto.rowVersion())
                .createTime(dto.createTime())
                .build();
    }
}
