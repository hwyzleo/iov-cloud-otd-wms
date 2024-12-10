package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.WarehousePo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台仓库转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface WarehouseMptAssembler {

    WarehouseMptAssembler INSTANCE = Mappers.getMapper(WarehouseMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param warehousePo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    WarehouseMpt fromPo(WarehousePo warehousePo);

    /**
     * 数据传输对象转数据对象
     *
     * @param warehouseMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    WarehousePo toPo(WarehouseMpt warehouseMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param warehousePoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<WarehouseMpt> fromPoList(List<WarehousePo> warehousePoList);

}
