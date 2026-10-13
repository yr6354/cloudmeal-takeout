package com.cloudmeal.takeout.order;

import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.enums.PayStatus;
import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 订单状态机：统一收口“这个状态能不能做这个操作”的判断。
 *
 * <p>重构前每个方法各写一遍 {@code status > 2}、{@code status.equals(3)} 之类的判断，
 * 现在全部委托给 {@link OrderStatus#canTransitTo}，新增状态或调整流转规则只需改一处。</p>
 */
@Component
public class OrderStateMachine {

    public OrderStatus currentStatus(Orders orders) {
        return OrderStatus.fromCode(orders.getStatus())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                        "未知的订单状态：" + orders.getStatus()));
    }

    /**
     * 校验订单能否流转到目标状态，不合法时抛出带上下文信息的业务异常。
     */
    public void assertTransitionAllowed(Orders orders, OrderStatus target) {
        if (orders == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        OrderStatus current = currentStatus(orders);
        if (!current.canTransitTo(target)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, String.format(
                    "订单当前状态为【%s】，不允许变更为【%s】", current.getDescription(), target.getDescription()));
        }
    }

    /** 支付状态校验：已支付的订单不能重复支付 */
    public void assertNotPaid(Orders orders) {
        if (PayStatus.PAID.getCode() == safeCode(orders.getPayStatus())) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }
    }

    public boolean isPaid(Orders orders) {
        return PayStatus.PAID.getCode() == safeCode(orders.getPayStatus());
    }

    private int safeCode(Integer code) {
        return code == null ? -1 : code;
    }
}
