package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 商家接单请求。
 */
@Data
public class OrdersConfirmDTO implements Serializable {

    @NotNull(message = "订单 id 不能为空")
    private Long id;

    /** 目标订单状态 */
    private Integer status;
}
