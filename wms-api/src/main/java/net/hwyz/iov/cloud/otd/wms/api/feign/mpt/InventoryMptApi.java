package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;

/**
 * 库存相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface InventoryMptApi {

    /**
     * 分页查询库存
     *
     * @param inventory 库存
     * @return 库存列表
     */
    TableDataInfo list(InventoryMpt inventory);

    /**
     * 导出库存
     *
     * @param response  响应
     * @param inventory 库存
     */
    void export(HttpServletResponse response, InventoryMpt inventory);

    /**
     * 根据库存ID获取库存
     *
     * @param inventoryId 库存ID
     * @return 库存
     */
    AjaxResult getInfo(Long inventoryId);

}
