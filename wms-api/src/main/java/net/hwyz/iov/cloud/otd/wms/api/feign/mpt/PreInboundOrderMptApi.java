package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
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
    TableDataInfo list(PreInboundOrderMpt preInboundOrder);

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
    AjaxResult getInfo(Long preInboundOrderId);

    /**
     * 新增预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    AjaxResult add(PreInboundOrderMpt preInboundOrder);

    /**
     * 修改预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    AjaxResult edit(PreInboundOrderMpt preInboundOrder);

    /**
     * 删除预入库单
     *
     * @param preInboundOrderIds 预入库单ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] preInboundOrderIds);

}
