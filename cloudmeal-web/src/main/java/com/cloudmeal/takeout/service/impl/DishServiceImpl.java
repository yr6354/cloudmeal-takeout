package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cloudmeal.takeout.cache.CacheService;
import com.cloudmeal.takeout.constant.CacheKeyConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.dto.DishDTO;
import com.cloudmeal.takeout.dto.DishPageQueryDTO;
import com.cloudmeal.takeout.entity.Dish;
import com.cloudmeal.takeout.entity.DishFlavor;
import com.cloudmeal.takeout.entity.Setmeal;
import com.cloudmeal.takeout.mapper.DishFlavorMapper;
import com.cloudmeal.takeout.mapper.DishMapper;
import com.cloudmeal.takeout.mapper.SetmealDishMapper;
import com.cloudmeal.takeout.mapper.SetmealMapper;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.service.DishService;
import com.cloudmeal.takeout.vo.DishVO;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 新增菜品以及保存口味
     * 
     * @param dishDTO
     */
    @Override
    @Transactional // 要么都成功，要么都失败
    public void saveWithFlavor(DishDTO dishDTO) {
        // 保存菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        // 获取菜品ID
        Long dishId = dish.getId();
        // 保存口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        evictDishCache();
    }

    /**
     * 分页查询菜品
     * 
     * @param dishPageQueryDTO
     */
    @Override
    public PageResult<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     * 
     * @param ids
     */
    @Override
    @Transactional // 一致性
    public void deleteBatch(List<Long> ids) {
        // 判断菜品是否能够删除 是否启用
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 抛出异常
                throw new BusinessException(ErrorCode.DISH_ON_SALE);
            }
        }
        // 是否被套餐引用
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 抛出异常
            throw new BusinessException(ErrorCode.DISH_BE_RELATED_BY_SETMEAL);
        }

        // // 删除菜品
        // for (Long id : ids) {
        //     // 删除菜品口味
        // }
        
        //批量删除菜品数据
        dishMapper.deleteByIds(ids);

        //批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

        evictDishCache();
    }

    /**
     * 根据id查询菜品
     * 
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品
        Dish dish = dishMapper.getById(id);
        // 根据id查询菜品口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 封装数据
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 更新菜品以及保存口味
     * 
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改菜品的基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 删除菜品的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 保存菜品的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        evictDishCache();
    }

    /**
     * 启用或禁用菜品
     * 
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);

        // 如果是禁用，需要将套餐中的菜品也禁用
        if (status == StatusConstant.DISABLE) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
        // 菜品状态变化会联动套餐，两处缓存都要失效
        evictDishCache();
    }

    /**
     * 根据菜品分类id查询菜品
     * 
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * 
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            // 根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /** 菜品变更后清理用户端菜单缓存 */
    private void evictDishCache() {
        cacheService.evictByPrefix(CacheKeyConstant.DISH_LIST_BY_CATEGORY);
        cacheService.evictByPrefix(CacheKeyConstant.SETMEAL_LIST_BY_CATEGORY);
    }
}
