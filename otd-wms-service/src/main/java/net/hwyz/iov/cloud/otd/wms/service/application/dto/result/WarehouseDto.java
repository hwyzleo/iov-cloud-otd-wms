package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 仓库查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record WarehouseDto(Long id, String code, String name, String manager, String warehouseLevel,
                           String provinceCode, String cityCode, String countyCode, String address,
                           String zipcode, String fax, String tel, String mobile, Boolean enable,
                           Integer sort, Date createTime) {
}
