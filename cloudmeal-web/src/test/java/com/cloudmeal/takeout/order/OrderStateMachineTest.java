package com.cloudmeal.takeout.order;

import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.enums.PayStatus;
import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单状态机单元测试：覆盖正常流转与非法流转。
 */
class OrderStateMachineTest {

    private final OrderStateMachine stateMachine = new OrderStateMachine();

    private Orders orderWith(OrderStatus status) {
        return Orders.builder().id(1L).status(status.getCode()).build();
    }

    @Test
    @DisplayName("待付款订单可以流转到待接单（支付成功）")
    void pendingPaymentCanBecomeToBeConfirmed() {
        assertDoesNotThrow(() -> stateMachine.assertTransitionAllowed(
                orderWith(OrderStatus.PENDING_PAYMENT), OrderStatus.TO_BE_CONFIRMED));
    }

    @Test
    @DisplayName("派送中的订单不能回退到已接单")
    void deliveryInProgressCannotGoBackwards() {
        BusinessException ex = assertThrows(BusinessException.class, () -> stateMachine.assertTransitionAllowed(
                orderWith(OrderStatus.DELIVERY_IN_PROGRESS), OrderStatus.CONFIRMED));
        assertEquals(ErrorCode.ORDER_STATUS_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("派送中"));
    }

    @Test
    @DisplayName("派送中的订单可以被商家取消")
    void deliveryInProgressCanBeCancelledByAdmin() {
        assertDoesNotThrow(() -> stateMachine.assertTransitionAllowed(
                orderWith(OrderStatus.DELIVERY_IN_PROGRESS), OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("已完成订单是终态，不允许再流转")
    void completedIsTerminal() {
        assertTrue(OrderStatus.COMPLETED.isTerminal());
        assertThrows(BusinessException.class, () -> stateMachine.assertTransitionAllowed(
                orderWith(OrderStatus.COMPLETED), OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("订单为空时按订单不存在处理")
    void nullOrderMeansNotFound() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> stateMachine.assertTransitionAllowed(null, OrderStatus.CONFIRMED));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("已支付订单不能重复支付")
    void paidOrderCannotBePaidTwice() {
        Orders paid = Orders.builder().id(1L).payStatus(PayStatus.PAID.getCode()).build();
        assertTrue(stateMachine.isPaid(paid));
        BusinessException ex = assertThrows(BusinessException.class, () -> stateMachine.assertNotPaid(paid));
        assertEquals(ErrorCode.ORDER_ALREADY_PAID, ex.getErrorCode());
    }

    @Test
    @DisplayName("未支付订单可以支付")
    void unpaidOrderCanBePaid() {
        Orders unpaid = Orders.builder().id(1L).payStatus(PayStatus.UN_PAID.getCode()).build();
        assertFalse(stateMachine.isPaid(unpaid));
        assertDoesNotThrow(() -> stateMachine.assertNotPaid(unpaid));
    }

    @Test
    @DisplayName("未知状态码给出明确异常而不是空指针")
    void unknownStatusCodeFailsFast() {
        Orders weird = Orders.builder().id(1L).status(99).build();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> stateMachine.currentStatus(weird));
        assertEquals(ErrorCode.ORDER_STATUS_ERROR, ex.getErrorCode());
    }
}
