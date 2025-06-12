package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.InventoryCountDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.InventoryCountDetailDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountPo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 盘点应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCountAppService {

    private final InventoryCountDao inventoryCountDao;
    private final InventoryCountDetailDao inventoryCountDetailDao;

    /**
     * 查询盘点信息
     *
     * @param orderNum       盘点单号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param state          盘点状态
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 盘点信息列表
     */
    public List<InventoryCountPo> search(String orderNum, String warehouseLevel, String warehouseCode, Integer state,
                                         Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNum", orderNum);
        map.put("warehouseLevel", warehouseLevel);
        map.put("warehouseCode", warehouseCode);
        map.put("state", state);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return inventoryCountDao.selectPoByMap(map);
    }

    /**
     * 查询盘点明细
     *
     * @param orderNum        盘点单号
     * @param warehouseCode   仓库代码
     * @param storageAreaCode 储区代码
     * @param beginTime       开始时间
     * @param endTime         结束时间
     * @return 盘点明细列表
     */
    public List<InventoryCountDetailPo> searchDetail(String orderNum, String warehouseCode, String storageAreaCode,
                                                     Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNum", orderNum);
        map.put("warehouseCode", warehouseCode);
        map.put("storageAreaCode", storageAreaCode);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return inventoryCountDetailDao.selectPoByMap(map);
    }

    /**
     * 根据主键ID获取盘点信息
     *
     * @param id 主键ID
     * @return 盘点信息
     */
    public InventoryCountPo getInventoryCountById(Long id) {
        return inventoryCountDao.selectPoById(id);
    }

    /**
     * 根据盘点单号及主键ID获取盘点明细
     *
     * @param id 主键ID
     * @return 盘点明细
     */
    public InventoryCountDetailPo getInventoryCountDetailByOrderNumAndId(String orderNum, Long id) {
        return inventoryCountDetailDao.selectPoByOrderNumAndId(orderNum, id);
    }

    /**
     * 新增盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    public int createInventoryCount(InventoryCountPo inventoryCount) {
        return inventoryCountDao.insertPo(inventoryCount);
    }

    /**
     * 新增盘点明细
     *
     * @param inventoryCountDetail 盘点明细
     * @return 结果
     */
    public int createInventoryCountDetail(InventoryCountDetailPo inventoryCountDetail) {
        return inventoryCountDetailDao.insertPo(inventoryCountDetail);
    }

    /**
     * 修改盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    public int modifyInventoryCount(InventoryCountPo inventoryCount) {
        return inventoryCountDao.updatePo(inventoryCount);
    }

    /**
     * 修改盘点明细
     *
     * @param inventoryCountDetail 盘点明细
     * @return 结果
     */
    public int modifyInventoryCountDetail(InventoryCountDetailPo inventoryCountDetail) {
        return inventoryCountDetailDao.updatePo(inventoryCountDetail);
    }

    /**
     * 批量删除盘点信息
     *
     * @param ids 盘点信息ID数组
     * @return 结果
     */
    public int deleteInventoryCountByIds(Long[] ids) {
        return inventoryCountDao.batchPhysicalDeletePo(ids);
    }

    /**
     * 批量删除盘点明细
     *
     * @param orderNum 盘点单号
     * @param ids      盘点明细ID数组
     * @return 结果
     */
    public int deleteInventoryCountDetailByIds(String orderNum, Long[] ids) {
        return inventoryCountDetailDao.batchPhysicalDeletePo(orderNum, ids);
    }

}
