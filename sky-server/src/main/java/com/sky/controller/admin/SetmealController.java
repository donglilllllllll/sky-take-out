package com.sky.controller.admin;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "管理端—浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @GetMapping("page")
    @ApiOperation(value = "套餐分页查询")
    public Result<PageResult<SetmealVO>> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        return Result.success(setmealService.pageList(setmealPageQueryDTO));

    }
    @PostMapping
    @ApiOperation(value = "新增套餐")
    @CacheEvict(cacheNames = "setmealCaches",key = "#setmealDTO.id")
    public Result insert(@RequestBody SetmealDTO setmealDTO) {
        setmealService.insert(setmealDTO);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getById(id));
    }

    @PutMapping
    @ApiOperation(value = "修改套餐")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("开始修改套餐======:{}", setmealDTO);
        setmealService.update(setmealDTO);
        log.info("修改修改套餐；结束=====");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "套餐起售、停售")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("套餐起售、停售：{}", status);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
    @DeleteMapping
    @ApiOperation(value = "批量删除套餐")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true)
    public Result batchDelete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.batchDelete(ids);
        return Result.success();
    }

}
