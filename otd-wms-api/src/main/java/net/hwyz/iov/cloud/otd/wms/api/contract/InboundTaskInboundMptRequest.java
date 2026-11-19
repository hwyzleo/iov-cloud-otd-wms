package net.hwyz.iov.cloud.otd.wms.api.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 管理后台待入库任务入库确认请求
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundTaskInboundMptRequest {

    /**
     * 请求幂等标识
     */
    @NotBlank(message = "请求标识不能为空")
    private String requestId;

    /**
     * 储区代码
     */
    @NotBlank(message = "储区不能为空")
    private String storageAreaCode;

    /**
     * 储位代码
     */
    @NotBlank(message = "储位不能为空")
    private String storageLocationCode;

    /**
     * 预期任务记录版本
     */
    private Integer expectedVersion;
}
