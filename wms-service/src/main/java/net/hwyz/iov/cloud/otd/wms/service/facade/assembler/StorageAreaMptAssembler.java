package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.StorageAreaMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageAreaPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台仓库储区转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface StorageAreaMptAssembler {

    StorageAreaMptAssembler INSTANCE = Mappers.getMapper(StorageAreaMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param storageAreaPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    StorageAreaMpt fromPo(StorageAreaPo storageAreaPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param storageAreaMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    StorageAreaPo toPo(StorageAreaMpt storageAreaMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param storageAreaPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<StorageAreaMpt> fromPoList(List<StorageAreaPo> storageAreaPoList);

}
