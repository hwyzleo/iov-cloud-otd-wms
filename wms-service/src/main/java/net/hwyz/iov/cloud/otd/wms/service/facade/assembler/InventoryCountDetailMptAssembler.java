package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountDetailMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountDetailPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台盘点明细转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryCountDetailMptAssembler {

    InventoryCountDetailMptAssembler INSTANCE = Mappers.getMapper(InventoryCountDetailMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param inventoryCountDetailPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    InventoryCountDetailMpt fromPo(InventoryCountDetailPo inventoryCountDetailPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param inventoryCountDetailMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    InventoryCountDetailPo toPo(InventoryCountDetailMpt inventoryCountDetailMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param inventoryCountDetailPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<InventoryCountDetailMpt> fromPoList(List<InventoryCountDetailPo> inventoryCountDetailPoList);

}
