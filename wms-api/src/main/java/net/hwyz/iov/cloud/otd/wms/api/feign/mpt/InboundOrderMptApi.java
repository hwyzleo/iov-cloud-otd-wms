package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;

/**
 * 入库单相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface InboundOrderMptApi {

    /**
     * 分页查询入库单
     *
     * @param inboundOrder 入库单
     * @return 入库单列表
     */
    TableDataInfo list(InboundOrderMpt inboundOrder);

    /**
     * 导出入库单
     *
     * @param response     响应
     * @param inboundOrder 入库单
     */
    void export(HttpServletResponse response, InboundOrderMpt inboundOrder);

    /**
     * 根据入库单ID获取入库单
     *
     * @param inboundOrderId 入库单ID
     * @return 入库单
     */
    AjaxResult getInfo(Long inboundOrderId);

    /**
     * 新增入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    AjaxResult add(InboundOrderMpt inboundOrder);

    /**
     * 修改入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    AjaxResult edit(InboundOrderMpt inboundOrder);

    /**
     * 删除入库单
     *
     * @param inboundOrderIds 入库单ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] inboundOrderIds);

}
