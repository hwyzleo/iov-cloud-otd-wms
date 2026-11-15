package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WmsOutboxMapper;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Outbox 发布仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final WmsOutboxMapper wmsOutboxMapper;

    @Override
    public void insert(OutboxRecord record) {
        wmsOutboxMapper.insert(PersistenceConverter.toPo(record));
    }

    @Override
    public List<OutboxRecord> listPublishable(Date now, int limit) {
        return wmsOutboxMapper.selectPublishable(now, limit).stream()
                .map(PersistenceConverter::toDomain)
                .toList();
    }

    @Override
    public int markPublished(Long id) {
        return wmsOutboxMapper.markPublished(id);
    }

    @Override
    public int markRetryable(Long id, int retryCount, Date nextRetryTime, String error) {
        return wmsOutboxMapper.markRetryable(id, retryCount, nextRetryTime, error);
    }

    @Override
    public int markDead(Long id, String error) {
        return wmsOutboxMapper.markDead(id, error);
    }
}
