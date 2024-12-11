package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.StorageLocationMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageLocationPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台仓库储位转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface StorageLocationMptAssembler {

    StorageLocationMptAssembler INSTANCE = Mappers.getMapper(StorageLocationMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param storageLocationPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    StorageLocationMpt fromPo(StorageLocationPo storageLocationPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param storageLocationMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    StorageLocationPo toPo(StorageLocationMpt storageLocationMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param storageLocationPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<StorageLocationMpt> fromPoList(List<StorageLocationPo> storageLocationPoList);

}
