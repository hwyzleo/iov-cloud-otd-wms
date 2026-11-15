package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageAreaDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageLocationDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.WarehouseDto;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.WarehouseMasterRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageAreaMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageLocationMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WarehouseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库主数据应用服务
 * <p>
 * 主数据维护保持既有行为，并增加层级归属校验与已完成事实的物理删除保护。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseAppService {

    private final WarehouseMapper warehouseMapper;
    private final StorageAreaMapper storageAreaMapper;
    private final StorageLocationMapper storageLocationMapper;
    private final WarehouseMasterRepository warehouseMasterRepository;

    /**
     * 查询仓库信息
     *
     * @param code           仓库代码
     * @param name           仓库名称
     * @param warehouseLevel 仓库体系层级
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 仓库列表
     */
    public List<WarehouseDto> search(String code, String name, String warehouseLevel, Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("name", ParamHelper.fuzzyQueryParam(name));
        map.put("warehouseLevel", warehouseLevel);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return WmsResultAssembler.fromWarehouseList(warehouseMapper.selectPoByMap(map));
    }

    /**
     * 根据仓库ID查询储区信息
     *
     * @param warehouseId 仓库ID
     * @return 储区列表
     */
    public List<StorageAreaDto> listStorageAreaByWarehouseId(Long warehouseId) {
        WarehousePo warehousePo = warehouseMapper.selectPoById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return List.of();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("warehouseCode", warehousePo.getCode());
        return WmsResultAssembler.fromStorageAreaList(storageAreaMapper.selectPoByMap(map));
    }

    /**
     * 根据仓库ID及储区ID查询储位信息
     *
     * @param warehouseId   仓库ID
     * @param storageAreaId 储区ID
     * @return 储位列表
     */
    public List<StorageLocationDto> listStorageLocationByWarehouseIdAndStorageAreaId(Long warehouseId,
                                                                                     Long storageAreaId) {
        WarehousePo warehousePo = warehouseMapper.selectPoById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return List.of();
        }
        StorageAreaPo storageAreaPo = storageAreaMapper.selectPoById(storageAreaId);
        if (ObjUtil.isNull(storageAreaPo)) {
            return List.of();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("warehouseCode", warehousePo.getCode());
        map.put("storageAreaCode", storageAreaPo.getCode());
        return WmsResultAssembler.fromStorageLocationList(storageLocationMapper.selectPoByMap(map));
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
        WarehousePo warehousePo = warehouseMapper.selectPoByCode(code);
        return !ObjUtil.isNotNull(warehousePo) || warehousePo.getId().longValue() == warehouseId.longValue();
    }

    /**
     * 检查储区代码在同一仓库内唯一
     *
     * @param storageAreaId 储区ID
     * @param storageArea   储区信息
     * @return 结果
     */
    public Boolean checkStorageAreaCodeUnique(Long storageAreaId, StorageAreaPo storageArea) {
        StorageAreaPo existing = storageAreaMapper.selectPoByWarehouseAndCode(storageArea.getWarehouseCode(),
                storageArea.getCode());
        if (existing == null) {
            return true;
        }
        return storageAreaId != null && existing.getId().longValue() == storageAreaId.longValue();
    }

    /**
     * 检查储位代码在同一仓库同一储区内唯一
     *
     * @param storageLocationId 储位ID
     * @param storageLocation   储位信息
     * @return 结果
     */
    public Boolean checkStorageLocationCodeUnique(Long storageLocationId, StorageLocationPo storageLocation) {
        StorageLocationPo existing = storageLocationMapper.selectPoByWarehouseAreaAndCode(
                storageLocation.getWarehouseCode(), storageLocation.getStorageAreaCode(), storageLocation.getCode());
        if (existing == null) {
            return true;
        }
        return storageLocationId != null && existing.getId().longValue() == storageLocationId.longValue();
    }

    /**
     * 根据主键ID获取仓库信息
     *
     * @param id 主键ID
     * @return 仓库信息
     */
    public WarehouseDto getWarehouseById(Long id) {
        return WmsResultAssembler.fromWarehouse(warehouseMapper.selectPoById(id));
    }

    /**
     * 根据仓库代码获取仓库信息
     *
     * @param code 仓库代码
     * @return 仓库信息
     */
    public WarehousePo getWarehouseByCode(String code) {
        return warehouseMapper.selectPoByCode(code);
    }

    /**
     * 新增仓库
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    public int createWarehouse(WarehousePo warehouse) {
        return warehouseMapper.insertPo(warehouse);
    }

    /**
     * 新增仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    public int createStorageArea(StorageAreaPo storageArea) {
        return storageAreaMapper.insertPo(storageArea);
    }

    /**
     * 新增仓库储位
     *
     * @param storageLocation 仓库储位
     * @return 结果
     */
    public int createStorageLocation(StorageLocationPo storageLocation) {
        return storageLocationMapper.insertPo(storageLocation);
    }

    /**
     * 修改仓库
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    public int modifyWarehouse(WarehousePo warehouse) {
        return warehouseMapper.updatePo(warehouse);
    }

    /**
     * 修改仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    public int modifyStorageArea(StorageAreaPo storageArea) {
        return storageAreaMapper.updatePo(storageArea);
    }

    /**
     * 修改仓库储位
     *
     * @param storageLocation 仓库储位
     * @return 结果
     */
    public int modifyStorageLocation(StorageLocationPo storageLocation) {
        return storageLocationMapper.updatePo(storageLocation);
    }

    /**
     * 批量删除仓库（被库存或已完成事实引用时拒绝）
     *
     * @param ids 仓库ID数组
     * @return 结果
     */
    public int deleteWarehouseByIds(Long[] ids) {
        for (Long id : ids) {
            WarehousePo warehousePo = warehouseMapper.selectPoById(id);
            if (ObjUtil.isNotNull(warehousePo) && warehouseMasterRepository.isWarehouseReferenced(warehousePo.getCode())) {
                throw WmsBusinessException.of(WmsErrorCode.WAREHOUSE_INVALID,
                        "仓库[" + warehousePo.getCode() + "]已被库存或已完成业务事实引用，禁止删除");
            }
        }
        return warehouseMapper.batchPhysicalDeletePo(ids);
    }

    /**
     * 批量删除储区（被引用时拒绝）
     *
     * @param warehouseId 仓库ID
     * @param ids         仓库储区ID数组
     * @return 结果
     */
    public int deleteStorageAreaByIds(Long warehouseId, Long[] ids) {
        WarehousePo warehousePo = warehouseMapper.selectPoById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return 0;
        }
        for (Long id : ids) {
            StorageAreaPo storageAreaPo = storageAreaMapper.selectPoById(id);
            if (ObjUtil.isNotNull(storageAreaPo)
                    && warehouseMasterRepository.isAreaReferenced(warehousePo.getCode(), storageAreaPo.getCode())) {
                throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                        "储区[" + storageAreaPo.getCode() + "]已被库存或已完成业务事实引用，禁止删除");
            }
        }
        return storageAreaMapper.batchPhysicalDeletePo(warehousePo.getCode(), ids);
    }

    /**
     * 批量删除储位（被引用时拒绝）
     *
     * @param warehouseId   仓库ID
     * @param storageAreaId 储区ID
     * @param ids           仓库储位ID数组
     * @return 结果
     */
    public int deleteStorageLocationByIds(Long warehouseId, Long storageAreaId, Long[] ids) {
        WarehousePo warehousePo = warehouseMapper.selectPoById(warehouseId);
        if (ObjUtil.isNull(warehousePo)) {
            return 0;
        }
        StorageAreaPo storageAreaPo = storageAreaMapper.selectPoById(storageAreaId);
        if (ObjUtil.isNull(storageAreaPo)) {
            return 0;
        }
        for (Long id : ids) {
            StorageLocationPo storageLocationPo = storageLocationMapper.selectPoById(id);
            if (ObjUtil.isNotNull(storageLocationPo)
                    && warehouseMasterRepository.isLocationReferenced(warehousePo.getCode(),
                    storageLocationPo.getCode())) {
                throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                        "储位[" + storageLocationPo.getCode() + "]已被库存或已完成业务事实引用，禁止删除");
            }
        }
        return storageLocationMapper.batchPhysicalDeletePo(warehousePo.getCode(),
                storageAreaPo.getCode(), ids);
    }
}
