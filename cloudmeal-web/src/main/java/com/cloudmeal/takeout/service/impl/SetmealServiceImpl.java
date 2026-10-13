package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
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
import com.cloudmeal.takeout.dto.SetmealDTO;
import com.cloudmeal.takeout.dto.SetmealPageQueryDTO;
import com.cloudmeal.takeout.entity.Dish;
import com.cloudmeal.takeout.entity.Setmeal;
import com.cloudmeal.takeout.entity.SetmealDish;
import com.cloudmeal.takeout.mapper.DishMapper;
import com.cloudmeal.takeout.mapper.SetmealDishMapper;
import com.cloudmeal.takeout.mapper.SetmealMapper;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.service.SetmealService;
import com.cloudmeal.takeout.vo.DishItemVO;
import com.cloudmeal.takeout.vo.SetmealVO;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 新增套餐
     * 
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 向套餐表插入数据
        setmealMapper.insert(setmeal);

        // 获取生成的套餐id
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        // 保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
        evictSetmealCache();
    }

    /**
     * 分页查询
     * 
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * 删除套餐
     * 
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new BusinessException(ErrorCode.SETMEAL_ON_SALE);
            }
        });

        // 删除套餐和菜品的关联关系
        ids.forEach(setmealID -> {
            // 删除套餐关联的菜品
            setmealDishMapper.deleteBySetmealId(setmealID);
            // 删除套餐
            setmealMapper.deleteById(setmealID);
        });
        evictSetmealCache();
    }

    /**
     * 根据id查询套餐
     * 
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        List<SetmealDish> dishBySetmealId = setmealDishMapper.getDishBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(dishBySetmealId);

        return setmealVO;
    }

    /**
     * 更新套餐
     * 
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 更新套餐
        setmealMapper.update(setmeal);
        // 更新套餐和菜品的关联关系
        Long setmealId = setmeal.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(sd -> {
            sd.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
        evictSetmealCache();
    }

    /**
     * 上架或下架
     * 
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if (status == StatusConstant.ENABLE) {
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new BusinessException(ErrorCode.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
        evictSetmealCache();
    }

    /**
     * 条件查询
     * 
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * 
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /** 套餐变更后清理用户端套餐缓存 */
    private void evictSetmealCache() {
        cacheService.evictByPrefix(CacheKeyConstant.SETMEAL_LIST_BY_CATEGORY);
        cacheService.evictByPrefix(CacheKeyConstant.SETMEAL_DETAIL);
    }
}
