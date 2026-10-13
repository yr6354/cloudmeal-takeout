package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 商家取消订单请求。
 */
@Data
public class OrdersCancelDTO implements Serializable {

    @NotNull(message = "订单 id 不能为空")
    private Long id;

    /** 取消原因 */
    private String cancelReason;
}
