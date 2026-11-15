package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;

/**
 * 消息消费结果
 *
 * @param outcome 消费结果
 * @param detail  处理说明
 * @author hwyz_leo
 */
public record ConsumeResultDto(ConsumeOutcome outcome, String detail) {

    public static ConsumeResultDto processed(String detail) {
        return new ConsumeResultDto(ConsumeOutcome.PROCESSED, detail);
    }

    public static ConsumeResultDto duplicate(String detail) {
        return new ConsumeResultDto(ConsumeOutcome.DUPLICATE, detail);
    }

    public static ConsumeResultDto rejected(String detail) {
        return new ConsumeResultDto(ConsumeOutcome.BUSINESS_REJECTED, detail);
    }

    public static ConsumeResultDto deadLetter(String detail) {
        return new ConsumeResultDto(ConsumeOutcome.DEAD_LETTER, detail);
    }
}
