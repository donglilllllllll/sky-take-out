package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface CategoryService extends IService<Category> {
    /**
     * 修改分类
     * @param categoryDTO
     */
    void updateCateDtoById(CategoryDTO categoryDTO);

   PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void insert(CategoryDTO categoryDTO);
}
