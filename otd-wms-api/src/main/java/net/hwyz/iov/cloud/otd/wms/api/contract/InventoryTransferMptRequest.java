package net.hwyz.iov.cloud.otd.wms.api.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 管理后台同仓移储请求
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransferMptRequest {

    /**
     * 请求幂等标识
     */
    @NotBlank(message = "请求标识不能为空")
    private String requestId;

    /**
     * 车架号
     */
    @NotBlank(message = "车架号不能为空")
    private String vin;

    /**
     * 仓库代码
     */
    @NotBlank(message = "仓库代码不能为空")
    private String warehouseCode;

    /**
     * 目标储区代码
     */
    @NotBlank(message = "目标储区代码不能为空")
    private String newStorageAreaCode;

    /**
     * 目标储位代码
     */
    @NotBlank(message = "目标储位代码不能为空")
    private String newStorageLocationCode;

    /**
     * 预期库存记录版本
     */
    private Integer expectedVersion;
}
