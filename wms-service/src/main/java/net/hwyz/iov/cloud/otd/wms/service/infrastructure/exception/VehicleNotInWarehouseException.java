package net.hwyz.iov.cloud.otd.wms.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 仓库库存中不存在指定车辆异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class VehicleNotInWarehouseException extends WmsBaseException {

    private static final int ERROR_CODE = 302001;

    public VehicleNotInWarehouseException(String warehouseCode, String vin) {
        super(ERROR_CODE);
        logger.warn("仓库[{}]库存中不存在车辆[{}]", warehouseCode, vin);
    }

}
