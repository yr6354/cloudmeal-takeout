package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单支付请求。
 */
@Data
public class OrdersPaymentDTO implements Serializable {

    @NotBlank(message = "订单号不能为空")
    private String orderNumber;

    private Integer payMethod;
}
