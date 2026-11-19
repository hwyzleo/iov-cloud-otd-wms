package net.hwyz.iov.cloud.otd.wms.service.domain.model.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * 入库单（不可变的已完成事实）
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class InboundOrderFact {

    /**
     * 主键
     */
    private Long id;
    /**
     * 入库单号
     */
    private String orderNum;
    /**
     * 预入库单号
     */
    private String preOrderNum;
    /**
     * 本次入库属于该 VIN 第几段
     */
    private Integer lifecycleSeq;
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
     * 储区代码
     */
    private String storageAreaCode;
    /**
     * 储位代码
     */
    private String storageLocationCode;
    /**
     * 入库时间
     */
    private Date inboundTime;
    /**
     * 入库者
     */
    private String inboundBy;
    /**
     * 创建时间
     */
    private Date createTime;
}
