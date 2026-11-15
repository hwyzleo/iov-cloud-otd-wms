package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountMpt;

/**
 * 盘点相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface InventoryCountMptApi {

    /**
     * 分页查询盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 盘点信息列表
     */
    ApiResponse<PageResult<InventoryCountMpt>> list(InventoryCountMpt inventoryCount);

    /**
     * 分页查询盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetail   盘点明细
     * @return 盘点明细列表
     */
    ApiResponse<PageResult<InventoryCountDetailMpt>> listDetail(String inventoryCountOrderNum,
                                                                InventoryCountDetailMpt inventoryCountDetail);

    /**
     * 导出盘点信息
     *
     * @param response       响应
     * @param inventoryCount 盘点信息
     */
    void export(HttpServletResponse response, InventoryCountMpt inventoryCount);

    /**
     * 根据盘点信息ID获取盘点信息
     *
     * @param inventoryCountId 盘点信息ID
     * @return 盘点信息
     */
    ApiResponse<InventoryCountMpt> getInfo(Long inventoryCountId);

    /**
     * 根据盘点明细ID获取盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetailId 盘点明细ID
     * @return 盘点明细
     */
    ApiResponse<InventoryCountDetailMpt> getDetailInfo(String inventoryCountOrderNum, Long inventoryCountDetailId);

    /**
     * 新增盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    ApiResponse<Void> add(InventoryCountMpt inventoryCount);

    /**
     * 新增盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetail   盘点明细
     * @return 结果
     */
    ApiResponse<Void> addDetail(String inventoryCountOrderNum, InventoryCountDetailMpt inventoryCountDetail);

    /**
     * 修改盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    ApiResponse<Void> edit(InventoryCountMpt inventoryCount);

    /**
     * 修改盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetail   盘点明细
     * @return 结果
     */
    ApiResponse<Void> editDetail(String inventoryCountOrderNum, InventoryCountDetailMpt inventoryCountDetail);

    /**
     * 删除盘点信息
     *
     * @param inventoryCountIds 盘点信息ID数组
     * @return 结果
     */
    ApiResponse<Void> remove(Long[] inventoryCountIds);

    /**
     * 删除盘点明细
     *
     * @param inventoryCountOrderNum  盘点单号
     * @param inventoryCountDetailIds 盘点明细ID数组
     * @return 结果
     */
    ApiResponse<Void> removeDetail(String inventoryCountOrderNum, Long[] inventoryCountDetailIds);

}
