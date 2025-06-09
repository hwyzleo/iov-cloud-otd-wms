package net.hwyz.iov.cloud.otd.wms.service.facade.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderExService;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 外部服务预入库单转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface PreInboundOrderExServiceAssembler {

    PreInboundOrderExServiceAssembler INSTANCE = Mappers.getMapper(PreInboundOrderExServiceAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param preInboundOrderPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    PreInboundOrderExService fromPo(PreInboundOrderPo preInboundOrderPo);

    /**
     * 数据传输对象转数据对象
     *
     * @param preInboundOrderExService 数据传输对象
     * @return 数据对象
     */
    @Mappings({})
    PreInboundOrderPo toPo(PreInboundOrderExService preInboundOrderExService);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param preInboundOrderPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<PreInboundOrderExService> fromPoList(List<PreInboundOrderPo> preInboundOrderPoList);

}
