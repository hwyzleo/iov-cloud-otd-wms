package net.hwyz.iov.cloud.otd.wms.service.common.enums;

import java.util.Arrays;

/**
 * 库存状态
 * <p>
 * 数值与数据库保持一致：1=AVAILABLE，2=OUTBOUND，3=RESERVED，4=IN_TRANSIT（交付库在途）。
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
    RESERVED(3),
    /**
     * 在途／待交付库入库（不占储位，目标仓库见 target_warehouse_code）
     */
    IN_TRANSIT(4);

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
