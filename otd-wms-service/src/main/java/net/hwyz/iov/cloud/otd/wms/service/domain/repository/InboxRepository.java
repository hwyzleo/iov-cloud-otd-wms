package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;

import java.util.Optional;

/**
 * Inbox 幂等仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface InboxRepository {

    /**
     * 查询已记录的处理结果
     *
     * @param consumerName 消费者名称
     * @param topic        Topic
     * @param eventId      事件 ID
     * @return Inbox 记录
     */
    Optional<InboxRecord> find(String consumerName, String topic, String eventId);

    /**
     * 写入处理结果（与业务写入同一本地事务）
     *
     * @param record Inbox 记录
     */
    void insert(InboxRecord record);
}
