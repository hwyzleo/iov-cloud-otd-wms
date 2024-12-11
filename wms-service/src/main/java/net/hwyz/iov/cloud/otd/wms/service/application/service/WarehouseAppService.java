package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.StorageAreaDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.StorageLocationDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.WarehouseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.WarehousePo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseAppService {

    private final WarehouseDao warehouseDao;
    private final StorageAreaDao storageAreaDao;
    private final StorageLocationDao storageLocationDao;

    /**
     * 查询仓库信息
     *
     * @param code           仓库代码
     * @param name           仓库名称
     * @param warehouseLevel 仓库体系层级
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 销售车型列表
     */
    public List<WarehousePo> search(String code, String name, String warehouseLevel, Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("name", ParamHelper.fuzzyQueryParam(name));
        map.put("warehouseLevel", warehouseLevel);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return warehouseDao.selectPoByMap(map);
    }

    /**
     * 根据仓库ID查询储区信息
     *
     * @param warehouseId 仓库ID
     * @return 储区列表
     */
    public List<StorageAreaPo> listStorageAreaByWarehouseId(Long warehouseId) {
        WarehousePo warehousePo = getWarehouseById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("warehouseCode", warehousePo.getCode());
        return storageAreaDao.selectPoByMap(map);
    }

    /**
     * 根据仓库ID及储区ID查询储位信息
     *
     * @param warehouseId   仓库ID
     * @param storageAreaId 储区ID
     * @return 储区列表
     */
    public List<StorageLocationPo> listStorageLocationByWarehouseIdAndStorageAreaId(Long warehouseId, Long storageAreaId) {
        WarehousePo warehousePo = getWarehouseById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return null;
        }
        StorageAreaPo storageAreaPo = storageAreaDao.selectPoById(storageAreaId);
        if (ObjUtil.isNull(storageAreaPo)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("warehouseCode", warehousePo.getCode());
        map.put("storageAreaCode", storageAreaPo.getCode());
        return storageLocationDao.selectPoByMap(map);
    }

    /**
     * 检查仓库代码是否唯一
     *
     * @param warehouseId 仓库ID
     * @param code        仓库代码
     * @return 结果
     */
    public Boolean checkCodeUnique(Long warehouseId, String code) {
        if (ObjUtil.isNull(warehouseId)) {
            warehouseId = -1L;
        }
        WarehousePo warehousePo = getWarehouseByCode(code);
        return !ObjUtil.isNotNull(warehousePo) || warehousePo.getId().longValue() == warehouseId.longValue();
    }

    /**
     * 根据主键ID获取仓库信息
     *
     * @param id 主键ID
     * @return 仓库信息
     */
    public WarehousePo getWarehouseById(Long id) {
        return warehouseDao.selectPoById(id);
    }

    /**
     * 根据仓库代码获取仓库信息
     *
     * @param code 仓库代码
     * @return 仓库信息
     */
    public WarehousePo getWarehouseByCode(String code) {
        return warehouseDao.selectPoByCode(code);
    }

    /**
     * 新增仓库
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    public int createWarehouse(WarehousePo warehouse) {
        return warehouseDao.insertPo(warehouse);
    }

    /**
     * 新增仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    public int createStorageArea(StorageAreaPo storageArea) {
        return storageAreaDao.insertPo(storageArea);
    }

    /**
     * 新增仓库储位
     *
     * @param storageLocation 仓库储位
     * @return 结果
     */
    public int createStorageLocation(StorageLocationPo storageLocation) {
        return storageLocationDao.insertPo(storageLocation);
    }

    /**
     * 修改仓库
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    public int modifyWarehouse(WarehousePo warehouse) {
        return warehouseDao.updatePo(warehouse);
    }

    /**
     * 修改仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    public int modifyStorageArea(StorageAreaPo storageArea) {
        return storageAreaDao.updatePo(storageArea);
    }

    /**
     * 修改仓库储位
     *
     * @param storageLocation 仓库储位
     * @return 结果
     */
    public int modifyStorageLocation(StorageLocationPo storageLocation) {
        return storageLocationDao.updatePo(storageLocation);
    }

    /**
     * 批量删除仓库
     *
     * @param ids 仓库ID数组
     * @return 结果
     */
    public int deleteWarehouseByIds(Long[] ids) {
        return warehouseDao.batchPhysicalDeletePo(ids);
    }

    /**
     * 批量删除储区
     *
     * @param warehouseId 仓库ID
     * @param ids         仓库储区ID数组
     * @return 结果
     */
    public int deleteStorageAreaByIds(Long warehouseId, Long[] ids) {
        WarehousePo warehousePo = getWarehouseById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return 0;
        }
        return storageAreaDao.batchPhysicalDeletePo(warehousePo.getCode(), ids);
    }

    /**
     * 批量删除储位
     *
     * @param warehouseId 仓库ID
     * @param ids         仓库储位ID数组
     * @return 结果
     */
    public int deleteStorageLocationByIds(Long warehouseId, Long storageAreaId, Long[] ids) {
        WarehousePo warehousePo = getWarehouseById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return 0;
        }
        StorageAreaPo storageAreaPo = storageAreaDao.selectPoById(storageAreaId);
        if (ObjUtil.isNull(storageAreaPo)) {
            return 0;
        }
        return storageLocationDao.batchPhysicalDeletePo(warehousePo.getCode(), storageAreaPo.getCode(), ids);
    }

}
