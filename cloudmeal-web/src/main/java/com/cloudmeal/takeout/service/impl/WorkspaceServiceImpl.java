package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.entity.Orders;
import com.cloudmeal.takeout.enums.OrderStatus;
import com.cloudmeal.takeout.mapper.DishMapper;
import com.cloudmeal.takeout.mapper.OrderMapper;
import com.cloudmeal.takeout.mapper.SetmealMapper;
import com.cloudmeal.takeout.mapper.UserMapper;
import com.cloudmeal.takeout.service.WorkspaceService;
import com.cloudmeal.takeout.vo.BusinessDataVO;
import com.cloudmeal.takeout.vo.DishOverViewVO;
import com.cloudmeal.takeout.vo.OrderOverViewVO;
import com.cloudmeal.takeout.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * 
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);

        // 查询总订单数
        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", OrderStatus.COMPLETED.getCode());
        // 营业额
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        // 有效订单数
        Integer validOrderCount = orderMapper.countByMap(map);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            // 订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            // 平均客单价
            unitPrice = turnover / validOrderCount;
        }

        // 新增用户数
        Integer newUsers = userMapper.countByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", OrderStatus.TO_BE_CONFIRMED.getCode());

        // 待接单
        Integer waitingOrders = orderMapper.countByMap(map);

        // 待派送
        map.put("status", OrderStatus.CONFIRMED.getCode());
        Integer deliveredOrders = orderMapper.countByMap(map);

        // 已完成
        map.put("status", OrderStatus.COMPLETED.getCode());
        Integer completedOrders = orderMapper.countByMap(map);

        // 已取消
        map.put("status", OrderStatus.CANCELLED.getCode());
        Integer cancelledOrders = orderMapper.countByMap(map);

        // 全部订单
        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        Map<String, Integer> map = new HashMap<>();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        Map<String, Integer> map = new HashMap<>();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
