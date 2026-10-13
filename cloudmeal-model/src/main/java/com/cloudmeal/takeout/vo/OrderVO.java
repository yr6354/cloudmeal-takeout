package com.cloudmeal.takeout.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import com.cloudmeal.takeout.entity.OrderDetail;
import com.cloudmeal.takeout.entity.Orders;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false) // 不使用父类属性
public class OrderVO extends Orders {

    // 订单菜品信息
    private String orderDishes;

    // 订单详情
    private List<OrderDetail> orderDetailList;

}
