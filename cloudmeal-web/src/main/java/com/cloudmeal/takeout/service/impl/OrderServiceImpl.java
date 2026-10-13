package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.context.UserContext;
import com.cloudmeal.takeout.dto.OrdersCancelDTO;
import com.cloudmeal.takeout.dto.OrdersConfirmDTO;
import com.cloudmeal.takeout.dto.OrdersPageQueryDTO;
import com.cloudmeal.takeout.dto.OrdersPaymentDTO;
import com.cloudmeal.takeout.dto.OrdersRejectionDTO;
import com.cloudmeal.takeout.dto.OrdersSubmitDTO;
import com.cloudmeal.takeout.entity.AddressBook;
import com.cloudmeal.takeout.entity.OrderDetail;
import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.entity.ShoppingCart;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.enums.PayStatus;
import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.mapper.AddressBookMapper;
import com.cloudmeal.takeout.mapper.OrderDetailMapper;
import com.cloudmeal.takeout.mapper.OrderMapper;
import com.cloudmeal.takeout.mapper.ShoppingCartMapper;
import com.cloudmeal.takeout.order.OrderAssembler;
import com.cloudmeal.takeout.order.OrderNotifier;
import com.cloudmeal.takeout.order.OrderNumberGenerator;
import com.cloudmeal.takeout.order.OrderStateMachine;
import com.cloudmeal.takeout.result.ErrorCode;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.service.OrderService;
import com.cloudmeal.takeout.utils.WeChatPayUtil;
import com.cloudmeal.takeout.vo.OrderPaymentVO;
import com.cloudmeal.takeout.vo.OrderStatisticsVO;
import com.cloudmeal.takeout.vo.OrderSubmitVO;
import com.cloudmeal.takeout.vo.OrderVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单业务编排。
 *
 * <p>重构前这是一个近 500 行、包含 15 个方法的巨类，状态判断、对象装配、对外通知混在一起。
 * 现在职责拆分为：</p>
 * <ul>
 *   <li>{@link OrderStateMachine}：状态流转合法性</li>
 *   <li>{@link OrderAssembler}：DTO / 实体 / VO 转换</li>
 *   <li>{@link OrderNumberGenerator}：订单号生成</li>
 *   <li>{@link OrderNotifier}：商家端实时通知</li>
 * </ul>
 * <p>本类只负责「按什么顺序调用谁」以及事务边界。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WeChatPayUtil weChatPayUtil;

    private final OrderAssembler orderAssembler;
    private final OrderStateMachine orderStateMachine;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderNotifier orderNotifier;

    /* ------------------------------------------------------------------
     * 用户端
     * ------------------------------------------------------------------ */

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = UserContext.requireCurrentId();

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new BusinessException(ErrorCode.ADDRESS_BOOK_IS_NULL);
        }

        List<ShoppingCart> cartList = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (CollectionUtils.isEmpty(cartList)) {
            throw new BusinessException(ErrorCode.SHOPPING_CART_IS_NULL);
        }

        Orders orders = orderAssembler.toNewOrder(
                ordersSubmitDTO, addressBook, userId, orderNumberGenerator.next());
        orderMapper.insert(orders);

        orderDetailMapper.insertBatch(orderAssembler.toOrderDetails(cartList, orders.getId()));
        shoppingCartMapper.deleteByUserId(userId);

        log.info("用户 {} 下单成功，订单号 {}", userId, orders.getNumber());
        return orderAssembler.toSubmitVO(orders);
    }

    /**
     * 订单支付。
     *
     * <p>当前为演示环境：不接入真实微信支付，直接按支付成功处理。
     * 接入真实支付时，此处改为向支付渠道发起预支付并返回支付参数，
     * 由异步回调触发 {@link #paySuccess(String)}。</p>
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return null;
    }

    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {
        Orders orders = orderMapper.getByNumber(outTradeNo);
        if (orders == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderStateMachine.assertNotPaid(orders);
        orderStateMachine.assertTransitionAllowed(orders, OrderStatus.TO_BE_CONFIRMED);

        orderMapper.update(Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.TO_BE_CONFIRMED.getCode())
                .payStatus(PayStatus.PAID.getCode())
                .checkoutTime(LocalDateTime.now())
                .build());

        orders.setStatus(OrderStatus.TO_BE_CONFIRMED.getCode());
        orderNotifier.notifyNewOrder(orders);
    }

    @Override
    public PageResult<OrderVO> pageQueryByUser(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO query = new OrdersPageQueryDTO();
        query.setUserId(UserContext.requireCurrentId());
        query.setStatus(status);

        Page<Orders> pageOrders = orderMapper.pageQuery(query);
        List<OrderVO> orderVOList = new ArrayList<>();
        if (pageOrders != null) {
            for (Orders orders : pageOrders) {
                orderVOList.add(orderAssembler.toOrderVO(
                        orders, orderDetailMapper.getByOrderId(orders.getId())));
            }
        }
        long total = pageOrders == null ? 0L : pageOrders.getTotal();
        return new PageResult<>(total, orderVOList);
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return orderAssembler.toOrderVO(orders, orderDetailMapper.getByOrderId(orders.getId()));
    }

    @Override
    @Transactional
    public void userCancelById(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderStateMachine.assertTransitionAllowed(orders, OrderStatus.CANCELLED);

        Orders update = Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.CANCELLED.getCode())
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .build();

        // 已支付的订单需要退款
        if (orderStateMachine.isPaid(orders)) {
            refund(orders);
            update.setPayStatus(PayStatus.REFUND.getCode());
        }
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Long userId = UserContext.requireCurrentId();
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        if (CollectionUtils.isEmpty(details)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        shoppingCartMapper.insertBatch(orderAssembler.toShoppingCartItems(details, userId));
    }

    /* ------------------------------------------------------------------
     * 管理端
     * ------------------------------------------------------------------ */

    @Override
    public PageResult<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                List<OrderDetail> details = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = orderAssembler.toOrderVO(orders, details);
                orderVO.setOrderDishes(orderAssembler.summarizeDishes(details));
                orderVOList.add(orderVO);
            }
        }
        return new PageResult<>(page.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO statisticsVO = new OrderStatisticsVO();
        statisticsVO.setToBeConfirmed(countByStatus(OrderStatus.TO_BE_CONFIRMED));
        statisticsVO.setConfirmed(countByStatus(OrderStatus.CONFIRMED));
        statisticsVO.setDeliveryInProgress(countByStatus(OrderStatus.DELIVERY_IN_PROGRESS));
        return statisticsVO;
    }

    @Override
    @Transactional
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        transition(ordersConfirmDTO.getId(), OrderStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = requireOrder(ordersRejectionDTO.getId());
        orderStateMachine.assertTransitionAllowed(orders, OrderStatus.CANCELLED);

        Orders update = Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.CANCELLED.getCode())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        if (orderStateMachine.isPaid(orders)) {
            refund(orders);
            update.setPayStatus(PayStatus.REFUND.getCode());
        }
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = requireOrder(ordersCancelDTO.getId());
        orderStateMachine.assertTransitionAllowed(orders, OrderStatus.CANCELLED);

        Orders update = Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.CANCELLED.getCode())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();

        if (orderStateMachine.isPaid(orders)) {
            refund(orders);
            update.setPayStatus(PayStatus.REFUND.getCode());
        }
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void delivery(Long id) {
        transition(id, OrderStatus.DELIVERY_IN_PROGRESS);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Orders orders = requireOrder(id);
        orderStateMachine.assertTransitionAllowed(orders, OrderStatus.COMPLETED);
        orderMapper.update(Orders.builder()
                .id(orders.getId())
                .status(OrderStatus.COMPLETED.getCode())
                .deliveryTime(LocalDateTime.now())
                .build());
    }

    @Override
    public void reminder(Long id) {
        Orders orders = requireOrder(id);
        orderNotifier.notifyUrgeOrder(orders);
    }

    /* ------------------------------------------------------------------
     * 内部方法
     * ------------------------------------------------------------------ */

    private void transition(Long orderId, OrderStatus target) {
        Orders orders = requireOrder(orderId);
        orderStateMachine.assertTransitionAllowed(orders, target);
        orderMapper.update(Orders.builder()
                .id(orders.getId())
                .status(target.getCode())
                .build());
    }

    private Orders requireOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return orders;
    }

    private Integer countByStatus(OrderStatus status) {
        Integer count = orderMapper.countStatus(status.getCode());
        return count == null ? 0 : count;
    }

    /**
     * 发起退款。
     *
     * <p>演示环境未配置微信支付商户信息，退款通道不可用；
     * 这里按「模拟退款成功」处理并记录告警，保证取消流程不被外部依赖阻断。</p>
     */
    private void refund(Orders orders) {
        try {
            String result = weChatPayUtil.refund(
                    orders.getNumber(), orders.getNumber(), orders.getAmount(), orders.getAmount());
            log.info("订单 {} 退款申请已提交：{}", orders.getNumber(), result);
        } catch (Exception ex) {
            log.warn("退款通道未配置或不支持，订单 {} 按模拟退款处理：{}",
                    orders.getNumber(), ex.getMessage());
        }
    }
}
