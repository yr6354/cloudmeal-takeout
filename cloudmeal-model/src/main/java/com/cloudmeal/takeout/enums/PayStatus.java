package com.cloudmeal.takeout.enums;

import java.util.Optional;

/**
 * 支付状态。
 */
public enum PayStatus {

    UN_PAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUND(2, "已退款");

    private final int code;
    private final String description;

    PayStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<PayStatus> fromCode(Integer code) {
        if (code == null) {
            return Optional.empty();
        }
        for (PayStatus status : values()) {
            if (status.code == code) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
