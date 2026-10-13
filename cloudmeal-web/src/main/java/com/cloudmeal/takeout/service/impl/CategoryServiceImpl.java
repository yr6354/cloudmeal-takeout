package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cloudmeal.takeout.cache.CacheService;
import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.dto.CategoryDTO;
import com.cloudmeal.takeout.dto.CategoryPageQueryDTO;
import com.cloudmeal.takeout.entity.Category;
import com.cloudmeal.takeout.mapper.CategoryMapper;
import com.cloudmeal.takeout.mapper.DishMapper;
import com.cloudmeal.takeout.mapper.SetmealMapper;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 分类业务层
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 新增分类
     * 
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        // 分类状态默认为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        // 设置创建时间、修改时间、创建人、修改人

        categoryMapper.insert(category);
        evictCategoryCache();
    }

    /**
     * 分页查询
     * 
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        // 下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     * 
     * @param id
     */
    public void deleteById(Long id) {
        // 查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            // 当前分类下有菜品，不能删除
            throw new BusinessException(ErrorCode.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            // 当前分类下有菜品，不能删除
            throw new BusinessException(ErrorCode.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 删除分类数据
        categoryMapper.deleteById(id);
        evictCategoryCache();
    }

    /**
     * 修改分类
     * 
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // 设置修改时间、修改人

        categoryMapper.update(category);
        evictCategoryCache();
    }

    /**
     * 启用、禁用分类
     * 
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                // .updateTime(LocalDateTime.now())
                // .updateUser(UserContext.getCurrentId())
                .build();
        categoryMapper.update(category);
        evictCategoryCache();
    }

    /**
     * 根据类型查询分类
     * 
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }

    /** 分类变更后清理用户端分类缓存 */
    private void evictCategoryCache() {
        cacheService.evictByPrefix(CacheKeyConstant.CATEGORY_LIST_BY_TYPE);
    }
}
