package com.cloudmeal.takeout.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 订单状态及其合法流转。
 *
 * <p>用枚举替代散落的魔法数字，并把“哪些流转是合法的”这一领域规则内聚在枚举里，
 * 上层只做查询、不重复判断，避免各处 status &gt; 2 之类的隐式约定。</p>
 */
public enum OrderStatus {

    /** 待付款 */
    PENDING_PAYMENT(1, "待付款"),
    /** 待接单 */
    TO_BE_CONFIRMED(2, "待接单"),
    /** 已接单 */
    CONFIRMED(3, "已接单"),
    /** 派送中 */
    DELIVERY_IN_PROGRESS(4, "派送中"),
    /** 已完成 */
    COMPLETED(5, "已完成"),
    /** 已取消 */
    CANCELLED(6, "已取消");

    private final int code;
    private final String description;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_PAYMENT, EnumSet.of(TO_BE_CONFIRMED, CANCELLED),
            TO_BE_CONFIRMED, EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED, EnumSet.of(DELIVERY_IN_PROGRESS, CANCELLED),
            DELIVERY_IN_PROGRESS, EnumSet.of(COMPLETED, CANCELLED),
            COMPLETED, EnumSet.noneOf(OrderStatus.class),
            CANCELLED, EnumSet.noneOf(OrderStatus.class));

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<OrderStatus> fromCode(Integer code) {
        if (code == null) {
            return Optional.empty();
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    /** 当前状态是否可以流转到 {@code target} */
    public boolean canTransitTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /** 终态：不再允许任何流转 */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
