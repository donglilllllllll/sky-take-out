package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {
@Autowired
private CategoryService categoryService;

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */

    @PutMapping
    @ApiOperation(value = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("员工编辑：{}",categoryDTO);
        categoryService.updateCateDtoById(categoryDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @param
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询：{}{}",categoryPageQueryDTO);
        return Result.success(categoryService.pageQuery(categoryPageQueryDTO));
    }


    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用或禁用")
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("启用或禁用：{}",status,id);
        categoryService.startOrStop(status,id);

        return Result.success();
    }

    /**
     * 新增分类
     * @param
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result insert (@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}",categoryDTO);
        categoryService.insert(categoryDTO);
        return Result.success();
    }





































}
