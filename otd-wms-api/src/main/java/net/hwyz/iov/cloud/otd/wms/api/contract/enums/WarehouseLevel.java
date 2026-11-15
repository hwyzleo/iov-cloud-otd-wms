package net.hwyz.iov.cloud.otd.wms.api.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 仓库体系层级枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum WarehouseLevel {

    /** 前置库 **/
    PDC,
    /** 中央库 **/
    CDC,
    /** 区域库 **/
    RDC,
    /** 经销商库 **/
    DDC;
    public static WarehouseLevel valOf(String val) {
        return Arrays.stream(WarehouseLevel.values())
                .filter(warehouseLevel -> warehouseLevel.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
