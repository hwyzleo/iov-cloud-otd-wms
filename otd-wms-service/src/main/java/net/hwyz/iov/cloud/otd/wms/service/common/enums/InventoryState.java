package net.hwyz.iov.cloud.otd.wms.service.common.enums;

import java.util.Arrays;

/**
 * 库存状态
 * <p>
 * 数值与数据库保持一致：1=AVAILABLE，2=OUTBOUND，3=RESERVED（新增值）。
 *
 * @author hwyz_leo
 */
public enum InventoryState {

    /**
     * 在库可用
     */
    AVAILABLE(1),
    /**
     * 已出库
     */
    OUTBOUND(2),
    /**
     * 已预留（占用储位）
     */
    RESERVED(3);

    private final int value;

    InventoryState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static InventoryState valOf(Integer value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(state -> state.value == value)
                .findFirst()
                .orElse(null);
    }
}
