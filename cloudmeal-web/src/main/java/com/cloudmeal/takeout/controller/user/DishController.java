package com.cloudmeal.takeout.controller.user;

import com.cloudmeal.takeout.cache.CacheService;
import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.entity.Dish;
import com.cloudmeal.takeout.result.Result;
import com.cloudmeal.takeout.service.DishService;
import com.cloudmeal.takeout.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * 用户端菜品查询（带缓存）。
 */
@RestController("userDishController")
@RequestMapping("/user/dish")
@RequiredArgsConstructor
public class DishController {

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final DishService dishService;
    private final CacheService cacheService;

    /**
     * 根据分类 id 查询起售中的菜品。
     *
     * <p>缓存键与失效统一由 {@link CacheKeyConstant} 管理：菜品发生增删改时，
     * 由 Service 层按前缀清理，避免用户端长时间读到旧菜单。</p>
     */
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        String cacheKey = CacheKeyConstant.DISH_LIST_BY_CATEGORY + categoryId;
        List<DishVO> cached = cacheService.get(cacheKey);
        if (!CollectionUtils.isEmpty(cached)) {
            return Result.success(cached);
        }

        Dish condition = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<DishVO> list = dishService.listWithFlavor(condition);
        if (!CollectionUtils.isEmpty(list)) {
            cacheService.put(cacheKey, list, CACHE_TTL);
        }
        return Result.success(list);
    }
}
