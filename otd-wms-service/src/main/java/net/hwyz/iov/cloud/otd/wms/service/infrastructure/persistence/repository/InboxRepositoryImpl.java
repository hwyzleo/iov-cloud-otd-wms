package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WmsInboxMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Inbox 幂等仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class InboxRepositoryImpl implements InboxRepository {

    private final WmsInboxMapper wmsInboxMapper;

    @Override
    public Optional<InboxRecord> find(String consumerName, String topic, String eventId) {
        return Optional.ofNullable(PersistenceConverter.toDomain(
                wmsInboxMapper.selectPoByEvent(consumerName, topic, eventId)));
    }

    @Override
    public void insert(InboxRecord record) {
        wmsInboxMapper.insert(PersistenceConverter.toPo(record));
    }
}
