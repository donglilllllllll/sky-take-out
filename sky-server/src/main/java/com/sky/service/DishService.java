package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.service
 * @Project：sky-take-out
 * @name：DishService
 * @Date：2024/8/16 11:27
 * @Filename：DishService
 */
public interface DishService {

    void saveWithFlavor(DishDTO dishDTO);

    PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteByIds(List<Long> ids);

    DishVO getById(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);

    List<Dish> dishList(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
