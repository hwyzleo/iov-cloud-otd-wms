package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskActionMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskInboundMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskMpt;

/**
 * 待入库任务相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface InboundTaskMptApi {

    /**
     * 分页查询待入库任务
     *
     * @param inboundTask 查询条件
     * @return 任务列表
     */
    ApiResponse<PageResult<InboundTaskMpt>> list(InboundTaskMpt inboundTask);

    /**
     * 根据任务ID获取待入库任务
     *
     * @param inboundTaskId 任务ID
     * @return 待入库任务
     */
    ApiResponse<InboundTaskMpt> getInfo(Long inboundTaskId);

    /**
     * 确认到货
     *
     * @param orderNum 任务号
     * @param request  到货请求
     * @return 待入库任务
     */
    ApiResponse<InboundTaskMpt> arrival(String orderNum, InboundTaskActionMptRequest request);

    /**
     * 确认入库
     *
     * @param orderNum 任务号
     * @param request  入库请求
     * @return 待入库任务
     */
    ApiResponse<InboundTaskMpt> inbound(String orderNum, InboundTaskInboundMptRequest request);

    /**
     * 取消待入库任务
     *
     * @param orderNum 任务号
     * @param request  取消请求
     * @return 待入库任务
     */
    ApiResponse<InboundTaskMpt> cancellation(String orderNum, InboundTaskCancellationMptRequest request);
}
