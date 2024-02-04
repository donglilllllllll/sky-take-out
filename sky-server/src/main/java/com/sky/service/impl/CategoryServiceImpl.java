package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
     private CategoryMapper categoryMapper;

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void updateCateDtoById(CategoryDTO categoryDTO) {
        Category category = categoryMapper.selectById(categoryDTO.getId());
        log.info("开始修改");
        if(category != null){
            Category category1 = new Category();
            BeanUtils.copyProperties(categoryDTO, category1);
            category1.setUpdateUser(BaseContext.getCurrentId());
            category1.setUpdateTime(LocalDateTime.now());
            categoryMapper.updateById(category1);
            log.info("修改成功");

        }else{
            log.info("没有查询到这个分类");

        }


    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {

        IPage<Category> page = new Page(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(categoryPageQueryDTO.getName() != null, Category::getName, categoryPageQueryDTO.getName())
                .like(categoryPageQueryDTO.getType() != null, Category::getType, categoryPageQueryDTO.getType())
                .orderByDesc(Category::getUpdateTime);

        IPage page1 = categoryMapper.selectPage(page, queryWrapper);

        return new PageResult(page1.getTotal(), page1.getRecords());

    }

    /**
     * 启用或禁用
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        //启用/禁用
        updateWrapper.set(Category::getStatus, status)
                .eq(Category::getId, id);

        categoryMapper.update(null, updateWrapper);

    }

    @Override
    public void insert(CategoryDTO categoryDTO) {
        Category category  = new Category();
        //对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        //设置账号状态为正常，默认正常为1，关闭为0
        category.setStatus(StatusConstant.ENABLE);

        //设置当前记录时间和最后一次修改时间
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateTime(LocalDateTime.now());
        // 记录当前创建人和修改人的ID 从当前线程获取
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }


}
