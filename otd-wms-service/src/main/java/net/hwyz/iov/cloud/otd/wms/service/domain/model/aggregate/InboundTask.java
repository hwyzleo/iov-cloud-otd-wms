package net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboundTaskState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;

import java.util.Date;

/**
 * 交付库待入库任务聚合根
 * <p>
 * 由通用到货请求命令创建（PENDING）；到货确认后 ARRIVED，入库确认后 COMPLETED；
 * 入库前可取消为 CANCELLED。每次接受的动作递增 {@code aggregateVersion}。
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class InboundTask {

    /**
     * 主键
     */
    private Long id;
    /**
     * 任务号
     */
    private String orderNum;
    /**
     * 到货请求幂等标识
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
     * 车架号
     */
    private String vin;
    /**
     * 生产配置代码
     */
    private String buildConfigCode;
    /**
     * 目标交付库代码
     */
    private String warehouseCode;
    /**
     * 任务状态
     */
    private InboundTaskState state;
    /**
     * 预计到达时间
     */
    private Date estimatedArrivalTime;
    /**
     * 到达时间
     */
    private Date arrivalTime;
    /**
     * 到达确认人
     */
    private String arrivalBy;
    /**
     * 入库完成时间
     */
    private Date inboundTime;
    /**
     * 入库确认人
     */
    private String inboundBy;
    /**
     * 取消时间
     */
    private Date cancelTime;
    /**
     * 取消人
     */
    private String cancelBy;
    /**
     * 取消原因
     */
    private String cancelReason;
    /**
     * 目标生命周期段次
     */
    private Integer lifecycleSeq;
    /**
     * 任务聚合版本
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
     * 创建待入库任务（PENDING）
     *
     * @param orderNum       任务号
     * @param requestId      请求标识
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param vin            车架号
     * @param buildConfigCode 生产配置代码
     * @param warehouseCode  目标交付库
     * @param estimatedArrivalTime 预计到达时间（可空）
     * @param lifecycleSeq   目标生命周期段次
     * @param createTime     创建时间
     * @return 待入库任务
     */
    public static InboundTask createPending(String orderNum, String requestId, String sourceSystem,
                                            String sourceOrderNum, String vin, String buildConfigCode,
                                            String warehouseCode, Date estimatedArrivalTime, int lifecycleSeq,
                                            Date createTime) {
        return InboundTask.builder()
                .orderNum(orderNum)
                .requestId(requestId)
                .sourceSystem(sourceSystem)
                .sourceOrderNum(sourceOrderNum)
                .vin(vin)
                .buildConfigCode(buildConfigCode)
                .warehouseCode(warehouseCode)
                .state(InboundTaskState.PENDING)
                .estimatedArrivalTime(estimatedArrivalTime)
                .lifecycleSeq(lifecycleSeq)
                .aggregateVersion(1L)
                .rowVersion(1)
                .createTime(createTime)
                .build();
    }

    /**
     * 到货确认：PENDING → ARRIVED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的到达时间
     */
    public void arrival(String operator, Date now) {
        if (state != InboundTaskState.PENDING) {
            throw WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                    "任务[" + orderNum + "]状态为[" + state + "]，不允许到货确认");
        }
        this.state = InboundTaskState.ARRIVED;
        this.arrivalTime = now;
        this.arrivalBy = operator;
        incrementVersion();
    }

    /**
     * 入库确认：ARRIVED → COMPLETED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的入库时间
     */
    public void inbound(String operator, Date now) {
        if (state != InboundTaskState.ARRIVED) {
            throw WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                    "任务[" + orderNum + "]状态为[" + state + "]，不允许入库确认");
        }
        this.state = InboundTaskState.COMPLETED;
        this.inboundTime = now;
        this.inboundBy = operator;
        incrementVersion();
    }

    /**
     * 取消：PENDING／ARRIVED → CANCELLED
     *
     * @param operator 可信操作人
     * @param reason   取消原因
     * @param now      WMS 生成的取消时间
     */
    public void cancel(String operator, String reason, Date now) {
        if (state != InboundTaskState.PENDING && state != InboundTaskState.ARRIVED) {
            throw WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                    "任务[" + orderNum + "]状态为[" + state + "]，不允许取消");
        }
        this.state = InboundTaskState.CANCELLED;
        this.cancelTime = now;
        this.cancelBy = operator;
        this.cancelReason = reason;
        incrementVersion();
    }

    private void incrementVersion() {
        this.aggregateVersion = this.aggregateVersion == null ? 1L : this.aggregateVersion + 1;
    }
}
