package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.service.impl
 * @Project：sky-take-out
 * @name：DishSeviceImpl
 * @Date：2024/6/23 21:06
 * @Filename：DishSeviceImpl
 */
@Service
@Slf4j
public class DishSeviceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //新增菜品信息到Dish表
        log.info("新增菜品，菜品信息：{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        log.info("dish的信息{}", dish);
        dishMapper.insert(dish);
        log.info("dish新增完成后的信息{}", dish);
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()) {
            //新增口味到DishFlavor表
            log.info("新增菜品，菜品口味信息：{}", dishDTO.getFlavors());
            dishDTO.getFlavors().forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());//关联dish菜品表的外键
                BeanUtils.copyProperties(dishDTO.getFlavors(), dishFlavor);
                dishFlavorMapper.insert(dishFlavor);

            });
        }

    }

    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageResult<DishVO> pageResult = new PageResult<>();
        IPage<Dish> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        log.info("分页查询菜品信息，分页参数：{}", dishPageQueryDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishPageQueryDTO, dish);
        dishMapper.selectPage(page, new LambdaQueryWrapper<Dish>()
                .like(StringUtils.isNotBlank(dish.getName()), Dish::getName, dish.getName()) //菜品名称模糊查询
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()) //分类id查询
                .eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus()) //状态查询
                .orderByDesc(Dish::getUpdateTime)
                .orderByDesc(Dish::getCreateTime)
        );
        List<DishVO> records = new ArrayList<>();
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            page.getRecords().forEach(item -> {
                DishVO dishVO = new DishVO();
                BeanUtils.copyProperties(item, dishVO);
                //分类名称
                Category category = categoryMapper.selectById(item.getCategoryId());
                if (category != null) {
                    dishVO.setCategoryName(category.getName());
                }
                //菜品口味
                List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                        .eq(item.getId() != null, DishFlavor::getDishId, item.getId())
                );
                if (dishFlavors != null && !dishFlavors.isEmpty()) {
                    dishVO.setFlavors(dishFlavors);
                }
                records.add(dishVO);
            });
        }
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(records);
        return pageResult;
    }

    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {
        //删除菜品表是否是起售中状态（起售状态，不能删除）
        List<Dish> dishes = dishMapper.selectBatchIds(ids);
        if (dishes != null && !dishes.isEmpty()) {
            dishes.forEach(item -> {
                if (item.getStatus() == 1) {
                    throw new BaseException(MessageConstant.DISH_ON_SALE);
                }
            });

            //删除菜品表
            dishMapper.deleteBatchIds(ids);
        }
        //套餐分类中是否关联菜品（如果套餐中关联菜品，则不能删除）
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .in(SetmealDish::getDishId, ids)
        );
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(item -> {
                if (item.getDishId() != null) {
                    throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
                }
            });
        }

        //删除菜品口味表
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                .in(DishFlavor::getDishId, ids)
        );
    }

    @Override
    public DishVO getById(Long id) {
        //查询菜品表
        Dish dish = dishMapper.selectById(id);
        if (dish != null) {
            log.info("菜品表======{}", dish);
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
           /* if (dish.getCategoryId() != null) {
                //根据菜品表的分类id查询分类表
                Category category = categoryMapper.selectById(dish.getCategoryId());
                if (category != null) {
                    log.info("菜品分类表======{}",category);
                    dishVO.setCategoryName(category.getName());
                }
            }*/
            if (dish.getId() != null) {
                //根据菜品表的id查询菜品口味表
                List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                        .eq(DishFlavor::getDishId, dish.getId())
                );
                if (dishFlavors != null && !dishFlavors.isEmpty()) {
                    log.info("菜品口味表======{}", dishFlavors);
                    dishVO.setFlavors(dishFlavors);
                }
            }
            return dishVO;
        }
        return null;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //新增菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);

        //新增口味表之前，删除与菜品关联的口味
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, dish.getId())
        );

        //然后在新增所有口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(item -> {
                //如果ID存在，
                item.setDishId(dish.getId());
                dishFlavorMapper.insert(item);
            });
        }
        //新增或更新口味表

    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dishMapper.update(dish, new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getId, id)
        );

    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(Dish::getStatus, 1) //起售状态
                .orderByDesc(Dish::getUpdateTime, Dish::getCreateTime)
        );

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, d.getId())
            );

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    @Override
    public List<Dish> dishList(Long categoryId) {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId));

    }


}
