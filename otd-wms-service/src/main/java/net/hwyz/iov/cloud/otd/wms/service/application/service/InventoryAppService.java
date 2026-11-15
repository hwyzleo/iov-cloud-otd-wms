package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存应用服务
 * <p>
 * 当前库存仅由入库、移储、预留、取消与出库业务动作维护，不提供通用写入口。
 *
 * @author hwyz_leo
 */
@Service
@RequiredArgsConstructor
public class InventoryAppService {

    private final InventoryRepository inventoryRepository;

    /**
     * 查询当前库存
     *
     * @param criteria 查询条件
     * @return 库存列表
     */
    public List<VehicleInventory> search(InventoryCriteria criteria) {
        return inventoryRepository.search(criteria);
    }

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 当前库存
     */
    public VehicleInventory getById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }
}
