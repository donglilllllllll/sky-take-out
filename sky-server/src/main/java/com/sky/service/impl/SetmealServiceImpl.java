package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;


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

    @Override
    public PageResult<SetmealVO> pageList(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult<SetmealVO> pageResult = new PageResult<>();
        Page<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        setmealMapper.selectPage(page, new LambdaQueryWrapper<Setmeal>()
                .eq(null != setmealPageQueryDTO.getCategoryId(), Setmeal::getCategoryId, setmealPageQueryDTO.getCategoryId())//套餐分类id
                .eq(StringUtils.isNotBlank(setmealPageQueryDTO.getName()), Setmeal::getName, setmealPageQueryDTO.getName())//套餐名称
                .eq(null != setmealPageQueryDTO.getStatus(), Setmeal::getStatus, setmealPageQueryDTO.getStatus())//套餐状态
                .orderByDesc(Setmeal::getUpdateTime)
        );
        List<SetmealVO> setmealVOS = new ArrayList<>();
        if (page.getRecords() != null) {
            page.getRecords().forEach(setmeal -> {
                SetmealVO setmealVO = new SetmealVO();
                BeanUtils.copyProperties(setmeal, setmealVO);
                setmealVO.setCategoryName(setmealMapper.getCategoryName(setmeal.getCategoryId()));
                log.info("setmealVO.CategoryName:{}", setmealVO.getCategoryName());
                setmealVOS.add(setmealVO);
            });
        }
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(setmealVOS);
        return pageResult;
    }

    @Override
    public void insert(SetmealDTO setmealDTO) {
        //新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //新增套餐菜品关系
        Long setmealId = setmeal.getId();
        log.info("setmealId:{}", setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
            setmealDishMapper.insert(setmealDish);
        });
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setCategoryName(setmealMapper.getCategoryName(setmeal.getCategoryId()));
        setmealVO.setSetmealDishes(setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, id)
        ));
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        log.info("更新setmeal表{}", setmeal);
        setmealMapper.updateById(setmeal);
        log.info("更新setmeal表结束");
        //删除套餐菜品关系
        log.info("删除setmeal_dish表{}", setmealDTO.getId());
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmealDTO.getId())
        );
        log.info("删除setmeal_dish表结束");
        log.info("新增setmeal_dish表{}", setmealDTO.getId());
        setmealDTO.getSetmealDishes().forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        });
        log.info("新增setmeal_dish表结束");
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        //更新套餐状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        log.info("更新setmeal表{}", setmeal);
        setmealMapper.updateById(setmeal);
        log.info("更新setmeal表结束");
    }

    @Override
    public void batchDelete(List<Long> ids) {
        log.info("批量删除setmeal表{}", ids);
        setmealMapper.deleteBatchIds(ids);
        log.info("批量删除setmeal表结束");
        log.info("批量删除setmeal_dish表{}", ids);
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId, ids)
        );
        log.info("批量删除setmeal_dish表结束");

    }
}
