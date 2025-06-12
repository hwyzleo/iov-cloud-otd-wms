package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryTransferPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台移库信息转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryTransferMptAssembler {

    InventoryTransferMptAssembler INSTANCE = Mappers.getMapper(InventoryTransferMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param inventoryTransferPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    InventoryTransferMpt fromPo(InventoryTransferPo inventoryTransferPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param inventoryTransferMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    InventoryTransferPo toPo(InventoryTransferMpt inventoryTransferMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param inventoryTransferPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<InventoryTransferMpt> fromPoList(List<InventoryTransferPo> inventoryTransferPoList);

}
