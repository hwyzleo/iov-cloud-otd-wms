package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundVinActionMptRequest;

import java.util.List;

/**
 * 出库单相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface OutboundOrderMptApi {

    /**
     * 分页查询出库单
     *
     * @param outboundOrder 出库单查询条件
     * @return 出库单列表
     */
    ApiResponse<PageResult<OutboundOrderMpt>> list(OutboundOrderMpt outboundOrder);

    /**
     * 根据出库单ID获取出库单
     *
     * @param outboundOrderId 出库单ID
     * @return 出库单
     */
    ApiResponse<OutboundOrderMpt> getInfo(Long outboundOrderId);

    /**
     * 获取出库单 VIN 明细
     *
     * @param outboundOrderId 出库单ID
     * @return VIN 明细列表
     */
    ApiResponse<List<OutboundOrderDetailMpt>> listDetail(Long outboundOrderId);

    /**
     * 确认装车
     *
     * @param orderNum 出库单号
     * @param vin      车架号
     * @param request  装车请求
     * @return 出库单明细
     */
    ApiResponse<OutboundOrderDetailMpt> loading(String orderNum, String vin, OutboundVinActionMptRequest request);

    /**
     * 确认出库
     *
     * @param orderNum 出库单号
     * @param vin      车架号
     * @param request  出库请求
     * @return 出库单明细
     */
    ApiResponse<OutboundOrderDetailMpt> outbound(String orderNum, String vin, OutboundVinActionMptRequest request);

    /**
     * 取消出库单
     *
     * @param orderNum 出库单号
     * @param request  取消请求
     * @return 出库单
     */
    ApiResponse<OutboundOrderMpt> cancellation(String orderNum, OutboundCancellationMptRequest request);

    /**
     * 导出出库单
     *
     * @param response      响应
     * @param outboundOrder 出库单查询条件
     */
    void export(HttpServletResponse response, OutboundOrderMpt outboundOrder);

}
