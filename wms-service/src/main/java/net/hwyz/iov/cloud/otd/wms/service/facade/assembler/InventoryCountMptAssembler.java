package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台盘点信息转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryCountMptAssembler {

    InventoryCountMptAssembler INSTANCE = Mappers.getMapper(InventoryCountMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param inventoryCountPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    InventoryCountMpt fromPo(InventoryCountPo inventoryCountPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param inventoryCountMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    InventoryCountPo toPo(InventoryCountMpt inventoryCountMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param inventoryCountPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<InventoryCountMpt> fromPoList(List<InventoryCountPo> inventoryCountPoList);

}
