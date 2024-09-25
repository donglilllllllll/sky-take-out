package com.sky.controller.user;

import com.sky.config.RedisConfiguration;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")

    public Result<List<DishVO>> list(Long categoryId) {
        //构造key值结构为dish_categoryId
        String key = "dish_" + categoryId;
        //查询redis缓存中是否存在菜品数据
        ValueOperations valueOperations = redisTemplate.opsForValue();
        List<DishVO> dishVOList = (List<DishVO>) valueOperations.get(key);
        //若果存在直接返回无需查询数据库
        if (dishVOList != null) {
            return Result.success(dishVOList);
        }
        //如果不存在，查询数据库，将查询到的数据库放入到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        //查询起售中的菜品
        dish.setStatus(StatusConstant.ENABLE);
        List<DishVO> list = dishService.listWithFlavor(dish);
        //将查询到的数据库放入到redis中
        valueOperations.set(key, list);
        return Result.success(list);
    }

}
