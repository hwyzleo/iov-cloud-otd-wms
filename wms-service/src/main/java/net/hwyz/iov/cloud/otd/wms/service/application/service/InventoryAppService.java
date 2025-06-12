package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.InventoryDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAppService {

    private final InventoryDao inventoryDao;

    /**
     * 库存状态：在库
     */
    private static final Integer STATE_INBOUND = 1;
    /**
     * 库存状态：出库
     */
    private static final Integer STATE_OUTBOUND = 2;

    /**
     * 查询库存
     *
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 入库单列表
     */
    public List<InventoryPo> search(String vin, String warehouseLevel, String warehouseCode, Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("vin", vin);
        map.put("warehouseLevel", warehouseLevel);
        map.put("warehouseCode", warehouseCode);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return inventoryDao.selectPoByMap(map);
    }

    /**
     * 根据主键ID获取库存
     *
     * @param id 主键ID
     * @return 库存
     */
    public InventoryPo getInventoryById(Long id) {
        return inventoryDao.selectPoById(id);
    }

    /**
     * 库存入库
     *
     * @param vin                 车架号
     * @param modelConfigCode     车型配置代码
     * @param warehouseCode       仓库代码
     * @param storageAreaCode     储区代码
     * @param storageLocationCode 储位代码
     * @param inboundTime         入库时间
     * @param inboundBy           入库者
     */
    public void inbound(String vin, String modelConfigCode, String warehouseCode, String storageAreaCode,
                        String storageLocationCode, Date inboundTime, Long inboundBy) {
        InventoryPo inventory = InventoryPo.builder()
                .vin(vin)
                .modelConfigCode(modelConfigCode)
                .warehouseCode(warehouseCode)
                .storageAreaCode(storageAreaCode)
                .storageLocationCode(storageLocationCode)
                .inboundTime(inboundTime)
                .inboundBy(inboundBy)
                .build();
        if (ObjUtil.isNull(inventory.getState())) {
            inventory.setState(STATE_INBOUND);
        }
        inventoryDao.insertPo(inventory);
    }

    /**
     * 修改库存
     *
     * @param inventory 库存
     * @return 结果
     */
    public int modifyInventory(InventoryPo inventory) {
        return inventoryDao.updatePo(inventory);
    }

    /**
     * 批量删除库存
     *
     * @param ids 库存ID数组
     * @return 结果
     */
    public int deleteInventoryByIds(Long[] ids) {
        return inventoryDao.batchPhysicalDeletePo(ids);
    }

}
