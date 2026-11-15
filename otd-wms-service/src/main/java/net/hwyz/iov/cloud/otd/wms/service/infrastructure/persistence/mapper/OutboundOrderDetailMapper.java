package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderDetailPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 出库单 VIN 明细 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface OutboundOrderDetailMapper extends BaseMapper<OutboundOrderDetailPo> {

}
