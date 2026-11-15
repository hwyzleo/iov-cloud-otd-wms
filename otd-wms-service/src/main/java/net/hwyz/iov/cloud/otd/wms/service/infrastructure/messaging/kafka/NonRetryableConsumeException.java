package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka;

/**
 * 不可重试的消费异常
 * <p>
 * 消息结构非法或确定性业务冲突：不进行有界重试，直接路由到消费者专属死信目的地。
 *
 * @author hwyz_leo
 */
public class NonRetryableConsumeException extends RuntimeException {

    public NonRetryableConsumeException(String message) {
        super(message);
    }

    public NonRetryableConsumeException(String message, Throwable cause) {
        super(message, cause);
    }
}
