package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.bean.BaseRequest;

import java.util.Date;

/**
 * 管理后台出库单
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OutboundOrderMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * WMS 出库单号
     */
    private String orderNum;

    /**
     * 首次出库请求幂等标识
     */
    private String requestId;

    /**
     * 通用来源系统
     */
    private String sourceSystem;

    /**
     * 来源单号
     */
    private String sourceOrderNum;

    /**
     * 已解析仓库代码
     */
    private String warehouseCode;

    /**
     * 出库单状态
     */
    private String state;

    /**
     * 出库聚合版本
     */
    private Long aggregateVersion;

    /**
     * 记录版本
     */
    private Integer rowVersion;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;
}
