package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;

/**
 * 入库单相关管理后台接口
 * <p>
 * 入库单为不可变的已完成事实：仅提供查询与导出，不提供通用新增／修改／删除。
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
    ApiResponse<PageResult<InboundOrderMpt>> list(InboundOrderMpt inboundOrder);

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
    ApiResponse<InboundOrderMpt> getInfo(Long inboundOrderId);

}
