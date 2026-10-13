package com.cloudmeal.takeout.controller.user;

import com.cloudmeal.takeout.cache.CacheService;
import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.entity.Category;
import com.cloudmeal.takeout.result.Result;
import com.cloudmeal.takeout.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * 用户端分类查询（带缓存）。
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@RequiredArgsConstructor
public class CategoryController {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final CategoryService categoryService;
    private final CacheService cacheService;

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        String cacheKey = CacheKeyConstant.CATEGORY_LIST_BY_TYPE + type;
        List<Category> cached = cacheService.get(cacheKey);
        if (!CollectionUtils.isEmpty(cached)) {
            return Result.success(cached);
        }

        List<Category> list = categoryService.list(type);
        if (!CollectionUtils.isEmpty(list)) {
            cacheService.put(cacheKey, list, CACHE_TTL);
        }
        return Result.success(list);
    }
}
