package net.hwyz.iov.cloud.otd.wms.service.domain.model.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * 移储记录（不可变的已完成事实）
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class InventoryTransferRecord {

    /**
     * 主键
     */
    private Long id;
    /**
     * 移储请求幂等标识
     */
    private String requestId;
    /**
     * 车架号
     */
    private String vin;
    /**
     * 生产配置代码
     */
    private String buildConfigCode;
    /**
     * 仓库代码
     */
    private String warehouseCode;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 仓库层级
     */
    private String warehouseLevel;
    /**
     * 原储区代码
     */
    private String oldStorageAreaCode;
    /**
     * 原储位代码
     */
    private String oldStorageLocationCode;
    /**
     * 新储区代码
     */
    private String newStorageAreaCode;
    /**
     * 新储位代码
     */
    private String newStorageLocationCode;
    /**
     * 移储时间
     */
    private Date transferTime;
    /**
     * 移储者
     */
    private String transferBy;
    /**
     * 创建时间
     */
    private Date createTime;
}
