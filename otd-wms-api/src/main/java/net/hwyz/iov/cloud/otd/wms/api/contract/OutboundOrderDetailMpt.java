package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;

import java.util.Date;

/**
 * 管理后台出库单 VIN 明细
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrderDetailMpt {

    /**
     * 主键
     */
    private Long id;

    /**
     * WMS 出库单号
     */
    private String orderNum;

    /**
     * 车架号
     */
    private String vin;

    /**
     * 生产配置代码
     */
    private String buildConfigCode;

    /**
     * 预留时仓库快照
     */
    private String warehouseCode;

    /**
     * 预留时储区快照
     */
    private String storageAreaCode;

    /**
     * 预留时储位快照
     */
    private String storageLocationCode;

    /**
     * 明细状态
     */
    private String state;

    /**
     * 预留时间
     */
    private Date reserveTime;

    /**
     * 装车时间
     */
    private Date loadTime;

    /**
     * 出库时间
     */
    private Date outboundTime;

    /**
     * 取消释放时间
     */
    private Date releaseTime;

    /**
     * 记录版本
     */
    private Integer rowVersion;
}
