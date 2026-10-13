package com.cloudmeal.takeout.controller.user;

import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端查询店铺营业状态。
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@RequiredArgsConstructor
public class ShopController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Object value = redisTemplate.opsForValue().get(CacheKeyConstant.SHOP_STATUS);
        Integer status = value instanceof Integer integer ? integer : StatusConstant.DISABLE;
        return Result.success(status);
    }
}
