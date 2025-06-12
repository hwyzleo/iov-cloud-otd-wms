package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;

/**
 * 移库相关管理后台接口
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
    TableDataInfo list(InventoryTransferMpt inventoryTransfer);

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
    AjaxResult getInfo(Long inventoryTransferId);

    /**
     * 新增移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    AjaxResult add(InventoryTransferMpt inventoryTransfer);

    /**
     * 修改移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    AjaxResult edit(InventoryTransferMpt inventoryTransfer);

    /**
     * 删除移库信息
     *
     * @param inventoryTransferIds 移库信息ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] inventoryTransferIds);

}
