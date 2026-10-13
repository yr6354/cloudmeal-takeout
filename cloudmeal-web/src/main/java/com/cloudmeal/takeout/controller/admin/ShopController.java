package com.cloudmeal.takeout.controller.admin;

import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺营业状态（存放于 Redis，管理端与用户端共用）。
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@RequiredArgsConstructor
public class ShopController {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 设置店铺状态：1 营业，0 休息 */
    @PutMapping("/{status}")
    public Result<String> setShopStatus(@PathVariable Integer status) {
        log.info("设置店铺状态：{}", status == 1 ? "营业" : "休息");
        redisTemplate.opsForValue().set(CacheKeyConstant.SHOP_STATUS, status);
        return Result.success();
    }

    /** 获取店铺状态：1 营业，0 休息 */
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Object value = redisTemplate.opsForValue().get(CacheKeyConstant.SHOP_STATUS);
        Integer status = value instanceof Integer integer ? integer : StatusConstant.DISABLE;
        return Result.success(status);
    }
}
