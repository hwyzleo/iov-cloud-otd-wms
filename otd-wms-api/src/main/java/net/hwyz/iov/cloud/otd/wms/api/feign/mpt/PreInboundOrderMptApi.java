package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundArrivalMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundInboundMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderMpt;

/**
 * 预入库单相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface PreInboundOrderMptApi {

    /**
     * 分页查询预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 预入库单列表
     */
    ApiResponse<PageResult<PreInboundOrderMpt>> list(PreInboundOrderMpt preInboundOrder);

    /**
     * 导出预入库单
     *
     * @param response        响应
     * @param preInboundOrder 预入库单
     */
    void export(HttpServletResponse response, PreInboundOrderMpt preInboundOrder);

    /**
     * 根据预入库单ID获取预入库单
     *
     * @param preInboundOrderId 预入库单ID
     * @return 预入库单
     */
    ApiResponse<PreInboundOrderMpt> getInfo(Long preInboundOrderId);

    /**
     * 确认到达
     *
     * @param orderNum 预入库单号
     * @param request  到达确认请求
     * @return 结果
     */
    ApiResponse<PreInboundOrderMpt> arrival(String orderNum, PreInboundArrivalMptRequest request);

    /**
     * 确认入库
     *
     * @param orderNum 预入库单号
     * @param request  入库确认请求
     * @return 结果
     */
    ApiResponse<PreInboundOrderMpt> inbound(String orderNum, PreInboundInboundMptRequest request);

    /**
     * 取消预入库单
     *
     * @param orderNum 预入库单号
     * @param request  取消请求
     * @return 结果
     */
    ApiResponse<PreInboundOrderMpt> cancellation(String orderNum, PreInboundCancellationMptRequest request);

}
