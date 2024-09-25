package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category>{
    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);

}

