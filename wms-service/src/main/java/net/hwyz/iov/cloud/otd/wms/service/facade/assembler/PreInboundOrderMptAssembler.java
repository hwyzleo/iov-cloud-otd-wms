package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台预入库单转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface PreInboundOrderMptAssembler {

    PreInboundOrderMptAssembler INSTANCE = Mappers.getMapper(PreInboundOrderMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param preInboundOrderPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    PreInboundOrderMpt fromPo(PreInboundOrderPo preInboundOrderPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param preInboundOrderMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    PreInboundOrderPo toPo(PreInboundOrderMpt preInboundOrderMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param preInboundOrderPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<PreInboundOrderMpt> fromPoList(List<PreInboundOrderPo> preInboundOrderPoList);

}
