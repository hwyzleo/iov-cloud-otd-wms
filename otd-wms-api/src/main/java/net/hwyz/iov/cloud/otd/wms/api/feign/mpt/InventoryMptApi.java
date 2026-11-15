package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;

/**
 * 库存相关管理后台接口
 * <p>
 * 库存仅由业务动作维护：不暴露通用新增／修改／删除能力。
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
    ApiResponse<PageResult<InventoryMpt>> list(InventoryMpt inventory);

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
    ApiResponse<InventoryMpt> getInfo(Long inventoryId);

}
