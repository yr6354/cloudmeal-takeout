package com.cloudmeal.takeout.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户下单请求。
 */
@Data
public class OrdersSubmitDTO implements Serializable {

    @NotNull(message = "收货地址不能为空")
    private Long addressBookId;

    @NotNull(message = "付款方式不能为空")
    private Integer payMethod;

    @Size(max = 100, message = "备注最长 100 个字符")
    private String remark;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    /** 配送状态 1 立即送出 0 选择具体时间 */
    private Integer deliveryStatus;

    /** 餐具数量 */
    private Integer tablewareNumber;

    /** 餐具数量状态 1 按餐量提供 0 选择具体数量 */
    private Integer tablewareStatus;

    /** 打包费 */
    private Integer packAmount;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于 0")
    private BigDecimal amount;
}
