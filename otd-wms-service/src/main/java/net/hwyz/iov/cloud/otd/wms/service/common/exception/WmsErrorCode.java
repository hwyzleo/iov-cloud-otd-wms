package net.hwyz.iov.cloud.otd.wms.service.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.hwyz.iov.cloud.framework.common.exception.ErrorCode;

/**
 * WMS 错误码注册表（单一事实来源）
 *
 * @author hwyz_leo
 */
@Getter
@AllArgsConstructor
public enum WmsErrorCode implements ErrorCode {

    /**
     * 基础错误
     */
    BASE("302000", "仓储管理业务处理失败"),
    /**
     * 车辆不在指定仓库
     */
    VEHICLE_NOT_IN_WAREHOUSE("302001", "车辆不在指定仓库"),
    /**
     * 仓库不存在、停用或归属不一致
     */
    WAREHOUSE_INVALID("302002", "仓库不存在、停用或归属不一致"),
    /**
     * 储区／储位不存在、停用或层级错误
     */
    STORAGE_LOCATION_INVALID("302003", "储区或储位不存在、停用或层级错误"),
    /**
     * 储位已占用
     */
    STORAGE_LOCATION_OCCUPIED("302004", "储位已占用"),
    /**
     * 预入库状态或版本冲突
     */
    PRE_INBOUND_STATE_CONFLICT("302005", "预入库单状态或版本冲突"),
    /**
     * VIN 不存在、重复或不是 AVAILABLE
     */
    VIN_NOT_AVAILABLE("302006", "车辆不存在、重复或不是在库可用状态"),
    /**
     * 出库单或明细状态冲突
     */
    OUTBOUND_STATE_CONFLICT("302007", "出库单或明细状态冲突"),
    /**
     * 请求标识被不同内容复用
     */
    REQUEST_ID_CONFLICT("302008", "请求标识被不同内容复用"),
    /**
     * 事件信封或消息结构非法
     */
    MESSAGE_INVALID("302009", "事件信封或消息结构非法"),
    /**
     * 整单出库预留被拒绝
     */
    OUTBOUND_RESERVATION_REJECTED("302010", "整单出库预留被拒绝"),
    /**
     * 并发修改或预期版本过期
     */
    STALE_VERSION("302011", "并发修改或预期版本过期"),
    /**
     * 入库任务状态冲突
     */
    INBOUND_TASK_STATE_CONFLICT("302012", "入库任务状态冲突"),
    /**
     * 车辆当前状态不可入库
     */
    VIN_NOT_INBOUNDABLE("302013", "车辆当前状态不可入库"),
    /**
     * 交付库目标仓库缺失或不匹配
     */
    INBOUND_WAREHOUSE_INVALID("302014", "交付库目标仓库缺失或层级不匹配");

    private final String code;
    private final String message;
}
