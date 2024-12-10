package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;

/**
 * 仓库相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface WarehouseMptApi {

    /**
     * 分页查询仓库信息
     *
     * @param warehouse 仓库信息
     * @return 仓库信息列表
     */
    TableDataInfo list(WarehouseMpt warehouse);

    /**
     * 导出仓库信息
     *
     * @param response  响应
     * @param warehouse 仓库信息
     */
    void export(HttpServletResponse response, WarehouseMpt warehouse);

    /**
     * 根据仓库ID获取仓库信息
     *
     * @param warehouseId 仓库ID
     * @return 仓库信息
     */
    AjaxResult getInfo(Long warehouseId);

    /**
     * 新增仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    AjaxResult add(WarehouseMpt warehouse);

    /**
     * 修改保存仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    AjaxResult edit(WarehouseMpt warehouse);

    /**
     * 删除仓库信息
     *
     * @param warehouseIds 仓库ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] warehouseIds);

}
