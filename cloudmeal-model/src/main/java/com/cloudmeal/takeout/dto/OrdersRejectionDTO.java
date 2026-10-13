package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 商家拒单请求。
 */
@Data
public class OrdersRejectionDTO implements Serializable {

    @NotNull(message = "订单 id 不能为空")
    private Long id;

    @NotBlank(message = "拒单原因不能为空")
    private String rejectionReason;
}
