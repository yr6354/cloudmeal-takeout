package com.cloudmeal.takeout.order;

import com.cloudmeal.takeout.dto.OrdersSubmitDTO;
import com.cloudmeal.takeout.entity.AddressBook;
import com.cloudmeal.takeout.entity.OrderDetail;
import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.entity.ShoppingCart;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.enums.PayStatus;
import com.cloudmeal.takeout.vo.OrderSubmitVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 订单装配器单元测试。
 */
class OrderAssemblerTest {

    private final OrderAssembler assembler = new OrderAssembler();

    private OrdersSubmitDTO submitDTO() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(7L);
        dto.setAmount(new BigDecimal("58.50"));
        dto.setPayMethod(1);
        dto.setRemark("不要香菜");
        return dto;
    }

    private AddressBook addressBook() {
        return AddressBook.builder()
                .id(7L)
                .consignee("张三")
                .phone("13800000000")
                .provinceName("北京市")
                .cityName("北京市")
                .districtName("海淀区")
                .detail("中关村大街 1 号")
                .build();
    }

    @Test
    @DisplayName("下单时补全订单号、用户、状态与收货信息")
    void buildNewOrder() {
        Orders orders = assembler.toNewOrder(submitDTO(), addressBook(), 100L, "202610131200000001");

        assertEquals("202610131200000001", orders.getNumber());
        assertEquals(100L, orders.getUserId());
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), orders.getStatus());
        assertEquals(PayStatus.UN_PAID.getCode(), orders.getPayStatus());
        assertEquals("张三", orders.getConsignee());
        assertEquals("13800000000", orders.getPhone());
        assertEquals("北京市北京市海淀区中关村大街 1 号", orders.getAddress());
        assertEquals(new BigDecimal("58.50"), orders.getAmount());
        assertEquals(0, orders.getPackAmount(), "打包费为空时应回退为 0");
    }

    @Test
    @DisplayName("购物车条目转换为订单明细并绑定订单 id")
    void buildOrderDetails() {
        ShoppingCart cart = ShoppingCart.builder()
                .id(11L)
                .name("宫保鸡丁")
                .number(2)
                .amount(new BigDecimal("30.00"))
                .build();

        List<OrderDetail> details = assembler.toOrderDetails(List.of(cart), 88L);

        assertEquals(1, details.size());
        OrderDetail detail = details.get(0);
        assertEquals(88L, detail.getOrderId());
        assertEquals("宫保鸡丁", detail.getName());
        assertEquals(2, detail.getNumber());
        assertNull(detail.getId(), "明细主键应由数据库生成，不应沿用购物车 id");
    }

    @Test
    @DisplayName("下单结果只暴露必要字段")
    void buildSubmitVO() {
        Orders orders = assembler.toNewOrder(submitDTO(), addressBook(), 100L, "202610131200000002");
        orders.setId(5L);

        OrderSubmitVO vo = assembler.toSubmitVO(orders);

        assertEquals(5L, vo.getId());
        assertEquals("202610131200000002", vo.getOrderNumber());
        assertEquals(new BigDecimal("58.50"), vo.getOrderAmount());
    }

    @Test
    @DisplayName("菜品摘要按「名称*数量；」拼接")
    void summarizeDishes() {
        List<OrderDetail> details = List.of(
                OrderDetail.builder().name("宫保鸡丁").number(3).build(),
                OrderDetail.builder().name("米饭").number(1).build());

        assertEquals("宫保鸡丁*3;米饭*1;", assembler.summarizeDishes(details));
        assertEquals("", assembler.summarizeDishes(List.of()));
    }

    @Test
    @DisplayName("再来一单：明细复制到购物车并清空原主键")
    void buildShoppingCartItems() {
        OrderDetail detail = OrderDetail.builder()
                .id(99L)
                .name("鱼香肉丝")
                .number(1)
                .amount(new BigDecimal("20.00"))
                .build();

        List<ShoppingCart> carts = assembler.toShoppingCartItems(List.of(detail), 100L);

        ShoppingCart cart = carts.get(0);
        assertNotSame(detail, cart);
        assertNull(cart.getId());
        assertEquals(100L, cart.getUserId());
        assertEquals("鱼香肉丝", cart.getName());
    }
}
