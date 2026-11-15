package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 储位查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record StorageLocationDto(Long id, String code, String name, String warehouseCode,
                                 String storageAreaCode, String manager, Boolean enable, Integer sort,
                                 Date createTime) {
}
