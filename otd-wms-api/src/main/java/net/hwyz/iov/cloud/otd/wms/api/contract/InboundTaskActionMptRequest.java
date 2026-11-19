package net.hwyz.iov.cloud.otd.wms.api.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 管理后台待入库任务到货／取消动作请求
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundTaskActionMptRequest {

    /**
     * 请求幂等标识
     */
    @NotBlank(message = "请求标识不能为空")
    private String requestId;

    /**
     * 预期任务记录版本
     */
    private Integer expectedVersion;
}
