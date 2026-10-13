package com.cloudmeal.takeout.order;

import com.cloudmeal.takeout.dto.OrdersSubmitDTO;
import com.cloudmeal.takeout.entity.AddressBook;
import com.cloudmeal.takeout.entity.OrderDetail;
import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.entity.ShoppingCart;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.enums.PayStatus;
import com.cloudmeal.takeout.vo.OrderSubmitVO;
import com.cloudmeal.takeout.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单对象装配器：负责「DTO / 实体 / VO」之间的转换。
 *
 * <p>把纯转换逻辑从 Service 里抽离，Service 只保留业务编排，转换逻辑也可被单元测试直接覆盖。</p>
 */
@Component
public class OrderAssembler {

    /** 由下单请求、收货地址生成待支付订单 */
    public Orders toNewOrder(OrdersSubmitDTO dto, AddressBook addressBook, Long userId, String orderNumber) {
        Orders orders = new Orders();
        // packAmount / tablewareNumber 在实体中是基本类型：请求未传时不能直接拷贝 null，
        // 否则 BeanUtils 会抛 FatalBeanException 导致下单失败，改用下面的显式兜底赋值。
        BeanUtils.copyProperties(dto, orders, "packAmount", "tablewareNumber");
        orders.setNumber(orderNumber);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orders.setPayStatus(PayStatus.UN_PAID.getCode());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(fullAddress(addressBook));
        orders.setPackAmount(dto.getPackAmount() == null ? 0 : dto.getPackAmount());
        orders.setTablewareNumber(dto.getTablewareNumber() == null ? 0 : dto.getTablewareNumber());
        return orders;
    }

    /** 购物车条目 → 订单明细 */
    public List<OrderDetail> toOrderDetails(List<ShoppingCart> cartList, Long orderId) {
        return cartList.stream().map(cart -> {
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(cart, detail);
            detail.setId(null);
            detail.setOrderId(orderId);
            return detail;
        }).collect(Collectors.toList());
    }

    public OrderSubmitVO toSubmitVO(Orders orders) {
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    public OrderVO toOrderVO(Orders orders, List<OrderDetail> details) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /** 拼接菜品摘要，例如「宫保鸡丁*3；米饭*1；」 */
    public String summarizeDishes(List<OrderDetail> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }
        return details.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber() + ";")
                .collect(Collectors.joining());
    }

    /** 历史订单明细 → 购物车（再来一单） */
    public List<ShoppingCart> toShoppingCartItems(List<OrderDetail> details, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return details.stream().map(detail -> {
            ShoppingCart cart = new ShoppingCart();
            BeanUtils.copyProperties(detail, cart, "id");
            cart.setUserId(userId);
            cart.setCreateTime(now);
            return cart;
        }).collect(Collectors.toList());
    }

    private String fullAddress(AddressBook addressBook) {
        return String.join("",
                nullToEmpty(addressBook.getProvinceName()),
                nullToEmpty(addressBook.getCityName()),
                nullToEmpty(addressBook.getDistrictName()),
                nullToEmpty(addressBook.getDetail()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
