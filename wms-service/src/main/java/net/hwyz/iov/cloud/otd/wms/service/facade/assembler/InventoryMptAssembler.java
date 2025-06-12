package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台库存转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryMptAssembler {

    InventoryMptAssembler INSTANCE = Mappers.getMapper(InventoryMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param inventoryPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    InventoryMpt fromPo(InventoryPo inventoryPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param inventoryMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    InventoryPo toPo(InventoryMpt inventoryMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param inventoryPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<InventoryMpt> fromPoList(List<InventoryPo> inventoryPoList);

}
