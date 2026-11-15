package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.common.util.DigestUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import org.springframework.stereotype.Service;

/**
 * MPT 业务动作幂等守卫
 * <p>
 * 以 {@code requestId} 作为动作幂等标识，与业务写入在同一本地事务中记录处理结果。
 *
 * @author hwyz_leo
 */
@Service
@RequiredArgsConstructor
public class MptActionGuard {

    private final InboxRepository inboxRepository;

    /**
     * 动作是否已处理
     *
     * @param action    动作标识
     * @param requestId 请求标识
     * @return 是否已处理
     */
    public boolean alreadyProcessed(String action, String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return false;
        }
        return inboxRepository.find(InboxRecords.MPT_ACTION_CONSUMER, InboxRecords.MPT_TOPIC, action + ":" + requestId)
                .isPresent();
    }

    /**
     * 记录动作处理成功（与业务写入同一事务）
     *
     * @param action    动作标识
     * @param requestId 请求标识
     * @param businessKey 业务键
     */
    public void recordProcessed(String action, String requestId, String businessKey) {
        if (requestId == null || requestId.isBlank()) {
            return;
        }
        String key = action + ":" + requestId;
        inboxRepository.insert(InboxRecords.processed(InboxRecords.MPT_ACTION_CONSUMER, InboxRecords.MPT_TOPIC,
                key, action, DigestUtil.sha256Hex(key), businessKey, null));
    }
}
