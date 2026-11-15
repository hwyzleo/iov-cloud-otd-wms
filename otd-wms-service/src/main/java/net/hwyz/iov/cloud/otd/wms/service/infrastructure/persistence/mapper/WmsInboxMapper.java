package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WmsInboxPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * WMS Inbox Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface WmsInboxMapper extends BaseMapper<WmsInboxPo> {

    /**
     * 按消费者、Topic、事件 ID 查询已记录结果
     *
     * @param consumerName 消费者名称
     * @param topic        Topic
     * @param eventId      事件 ID
     * @return Inbox 记录
     */
    WmsInboxPo selectPoByEvent(@Param("consumerName") String consumerName,
                               @Param("topic") String topic,
                               @Param("eventId") String eventId);

}
