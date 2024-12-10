package net.hwyz.iov.cloud.otd.wms.service.application.service;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao.WarehouseDao;
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
     * 修改仓库
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    public int modifyWarehouse(WarehousePo warehouse) {
        return warehouseDao.updatePo(warehouse);
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

}
