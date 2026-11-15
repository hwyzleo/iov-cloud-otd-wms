package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WmsOutboxPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * WMS Outbox Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface WmsOutboxMapper extends BaseMapper<WmsOutboxPo> {

    /**
     * 扫描可发布记录（PENDING / FAILED_RETRYABLE 且到达重试时间）
     *
     * @param now   当前时间
     * @param limit 批大小
     * @return 待发布记录
     */
    List<WmsOutboxPo> selectPublishable(@Param("now") Date now, @Param("limit") int limit);

    /**
     * 标记已发布
     *
     * @param id 主键
     * @return 影响行数
     */
    int markPublished(@Param("id") Long id);

    /**
     * 标记可重试
     *
     * @param id            主键
     * @param retryCount    已重试次数
     * @param nextRetryTime 下次重试时间
     * @param error         脱敏错误
     * @return 影响行数
     */
    int markRetryable(@Param("id") Long id, @Param("retryCount") int retryCount,
                      @Param("nextRetryTime") Date nextRetryTime, @Param("error") String error);

    /**
     * 标记终态失败
     *
     * @param id    主键
     * @param error 脱敏错误
     * @return 影响行数
     */
    int markDead(@Param("id") Long id, @Param("error") String error);

}
