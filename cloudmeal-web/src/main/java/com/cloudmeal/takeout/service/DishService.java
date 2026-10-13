package com.cloudmeal.takeout.service;

import java.util.List;

import com.cloudmeal.takeout.dto.DishDTO;
import com.cloudmeal.takeout.dto.DishPageQueryDTO;
import com.cloudmeal.takeout.entity.Dish;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.vo.DishVO;

public interface DishService {
    /**
     * 新增菜品以及保存口味
     * 
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * 
     * @param dishPageQueryDTO
     */
    public PageResult<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     * 
     * @param ids
     */
    public void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品
     * 
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id);

    /**
     * 更新菜品以及口味
     * 
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO);

    /**
     * 启用或禁用菜品
     * 
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * 
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     * 
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
