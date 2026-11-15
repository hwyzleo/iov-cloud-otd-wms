package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

/**
 * 逐 VIN 拒绝原因
 *
 * @param vin        车架号
 * @param reasonCode 错误码
 * @author hwyz_leo
 */
public record ItemReasonDto(String vin, String reasonCode) {
}
