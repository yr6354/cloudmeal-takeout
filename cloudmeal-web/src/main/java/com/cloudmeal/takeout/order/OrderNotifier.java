package com.cloudmeal.takeout.order;

import com.alibaba.fastjson.JSON;
import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单实时通知：把「来单 / 催单」推送给商家端。
 *
 * <p>消息体格式集中在此处定义，避免在业务代码里到处拼 Map + JSON。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotifier {

    /** 1：来单提醒 */
    public static final int TYPE_NEW_ORDER = 1;
    /** 2：客户催单 */
    public static final int TYPE_URGE_ORDER = 2;

    private final WebSocketServer webSocketServer;

    public void notifyNewOrder(Orders orders) {
        send(TYPE_NEW_ORDER, orders);
    }

    public void notifyUrgeOrder(Orders orders) {
        send(TYPE_URGE_ORDER, orders);
    }

    private void send(int type, Orders orders) {
        Map<String, Object> payload = new HashMap<>(4);
        payload.put("type", type);
        payload.put("orderId", orders.getId());
        payload.put("content", "订单号：" + orders.getNumber());
        String message = JSON.toJSONString(payload);
        log.info("推送订单消息：{}", message);
        webSocketServer.sendToAllClient(message);
    }
}
