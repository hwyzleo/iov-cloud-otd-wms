package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InboundOrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台入库单转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface InboundOrderMptAssembler {

    InboundOrderMptAssembler INSTANCE = Mappers.getMapper(InboundOrderMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param inboundOrderPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    InboundOrderMpt fromPo(InboundOrderPo inboundOrderPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param inboundOrderMpt 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    InboundOrderPo toPo(InboundOrderMpt inboundOrderMpt);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param inboundOrderPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<InboundOrderMpt> fromPoList(List<InboundOrderPo> inboundOrderPoList);

}
