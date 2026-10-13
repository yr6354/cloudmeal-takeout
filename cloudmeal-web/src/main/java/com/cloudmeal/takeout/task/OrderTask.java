package com.cloudmeal.takeout.task;

import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务：超时未支付自动取消、派送超时自动完成。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTask {

    /** 下单后多久未支付视为超时 */
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    /** 派送中多久未确认收货视为已完成 */
    private static final int DELIVERY_TIMEOUT_HOURS = 1;

    private final OrderMapper orderMapper;

    /** 每分钟扫描一次超时未支付的订单 */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<Orders> timeoutOrders =
                orderMapper.getByStatusAndOrderTimeLT(OrderStatus.PENDING_PAYMENT.getCode(), deadline);
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }
        timeoutOrders.forEach(orders -> orderMapper.update(Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.CANCELLED.getCode())
                .cancelReason("超时未支付，系统自动取消")
                .cancelTime(LocalDateTime.now())
                .build()));
        log.info("自动取消超时未支付订单 {} 笔", timeoutOrders.size());
    }

    /** 每天凌晨 1 点把派送超时的订单置为已完成 */
    @Scheduled(cron = "0 0 1 * * ?")
    public void completeTimeoutDeliveries() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(DELIVERY_TIMEOUT_HOURS);
        List<Orders> timeoutOrders =
                orderMapper.getByStatusAndOrderTimeLT(OrderStatus.DELIVERY_IN_PROGRESS.getCode(), deadline);
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }
        timeoutOrders.forEach(orders -> orderMapper.update(Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.COMPLETED.getCode())
                .deliveryTime(LocalDateTime.now())
                .build()));
        log.info("自动完成派送超时订单 {} 笔", timeoutOrders.size());
    }
}
