package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMptRequest;

/**
 * 移库相关管理后台接口
 * <p>
 * 移储为不可变的已完成事实：仅提供查询、导出与同仓移储动作，不提供通用修改／删除。
 *
 * @author hwyz_leo
 */
public interface InventoryTransferMptApi {

    /**
     * 分页查询移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 移库信息列表
     */
    ApiResponse<PageResult<InventoryTransferMpt>> list(InventoryTransferMpt inventoryTransfer);

    /**
     * 导出移库信息
     *
     * @param response          响应
     * @param inventoryTransfer 移库信息
     */
    void export(HttpServletResponse response, InventoryTransferMpt inventoryTransfer);

    /**
     * 根据移库信息ID获取移库信息
     *
     * @param inventoryTransferId 移库信息ID
     * @return 移库信息
     */
    ApiResponse<InventoryTransferMpt> getInfo(Long inventoryTransferId);

    /**
     * 同仓移储
     *
     * @param request 移储请求
     * @return 移库信息
     */
    ApiResponse<InventoryTransferMpt> transfer(InventoryTransferMptRequest request);

}
