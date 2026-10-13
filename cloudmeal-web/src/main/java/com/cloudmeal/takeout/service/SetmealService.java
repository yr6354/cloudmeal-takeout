package com.cloudmeal.takeout.service;

import java.util.List;

import com.cloudmeal.takeout.dto.SetmealDTO;
import com.cloudmeal.takeout.dto.SetmealPageQueryDTO;
import com.cloudmeal.takeout.entity.Setmeal;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.vo.DishItemVO;
import com.cloudmeal.takeout.vo.SetmealVO;

public interface SetmealService {

    /**
     * 新增套餐
     * 
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 分页查询
     * 
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 删除套餐
     * 
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * 
     * @param id
     * @return
     */
    SetmealVO getById(Long id);

    /**
     * 更新套餐
     * 
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 启用或禁用套餐
     * 
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * 
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * 
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

}
