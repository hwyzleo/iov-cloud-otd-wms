package net.hwyz.iov.cloud.otd.wms.api.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 管理后台按 VIN 装车 / 出库确认请求
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundVinActionMptRequest {

    /**
     * 请求幂等标识
     */
    @NotBlank(message = "请求标识不能为空")
    private String requestId;

    /**
     * 预期明细记录版本
     */
    private Integer expectedVersion;
}
