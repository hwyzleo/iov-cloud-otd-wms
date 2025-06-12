package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.exception.VehicleNotInWarehouseException;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.InventoryTransferDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryTransferPo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移库应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransferAppService {

    private final InventoryTransferDao inventoryTransferDao;
    private final InventoryAppService inventoryAppService;

    /**
     * 查询移库信息
     *
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 移库信息列表
     */
    public List<InventoryTransferPo> search(String vin, String warehouseLevel, String warehouseCode, Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("vin", vin);
        map.put("warehouseLevel", warehouseLevel);
        map.put("warehouseCode", warehouseCode);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return inventoryTransferDao.selectPoByMap(map);
    }

    /**
     * 根据主键ID获取移库信息
     *
     * @param id 主键ID
     * @return 移库信息
     */
    public InventoryTransferPo getInventoryTransferById(Long id) {
        return inventoryTransferDao.selectPoById(id);
    }

    /**
     * 新增移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    public int createInventoryTransfer(InventoryTransferPo inventoryTransfer) {
        InventoryPo inventory = inventoryAppService.getInventoryByWarehouseAndVin(inventoryTransfer.getWarehouseCode(), inventoryTransfer.getVin());
        if (ObjUtil.isNull(inventory)) {
            throw new VehicleNotInWarehouseException(inventoryTransfer.getWarehouseCode(), inventoryTransfer.getVin());
        }
        int result = inventoryTransferDao.insertPo(inventoryTransfer);
        inventory.setStorageAreaCode(inventoryTransfer.getNewStorageAreaCode());
        inventory.setStorageLocationCode(inventoryTransfer.getNewStorageLocationCode());
        inventory.setModifyBy(inventoryTransfer.getCreateBy());
        inventoryAppService.modifyInventory(inventory);
        return result;
    }

    /**
     * 修改移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    public int modifyInventoryTransfer(InventoryTransferPo inventoryTransfer) {
        InventoryPo inventory = inventoryAppService.getInventoryByWarehouseAndVin(inventoryTransfer.getWarehouseCode(), inventoryTransfer.getVin());
        if (ObjUtil.isNull(inventory)) {
            throw new VehicleNotInWarehouseException(inventoryTransfer.getWarehouseCode(), inventoryTransfer.getVin());
        }
        int result = inventoryTransferDao.updatePo(inventoryTransfer);
        inventory.setStorageAreaCode(inventoryTransfer.getNewStorageAreaCode());
        inventory.setStorageLocationCode(inventoryTransfer.getNewStorageLocationCode());
        inventory.setModifyBy(inventoryTransfer.getModifyBy());
        inventoryAppService.modifyInventory(inventory);
        return result;
    }

    /**
     * 批量删除移库信息
     *
     * @param ids 移库信息ID数组
     * @return 结果
     */
    public int deleteInventoryTransferByIds(Long[] ids) {
        return inventoryTransferDao.batchPhysicalDeletePo(ids);
    }

}
