package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.controller.admin
 * @Project：sky-take-out
 * @name：菜品管理
 * @Date：2024/6/23 20:58
 * @Filename：DishController
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清理以dish_CategoryId开头的key
        clearCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dishPageQueryDTO) throws IOException {
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult<DishVO> dishVOPageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(dishVOPageResult);
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping
    public Result batchDelete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品{}", ids);
        dishService.deleteByIds(ids);
        // 清理所有菜品的缓存数据,所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品詳情
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品詳情{}", id);
        return Result.success(dishService.getById(id));
    }

    /**
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 清理所有菜品的缓存数据,所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }

    /**
     * 起售停售
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        // 清理所有菜品的缓存数据,所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }
    @GetMapping("list")
    public Result<List<Dish>> list(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品{}",categoryId);
        List<Dish> dishList = dishService.dishList(categoryId);
        return Result.success(dishList);
    }
    /**
     * 清理缓存数据
     */
    public void clearCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

//    /**
//     * 文件下载（失败了会返回一个有部分数据的Excel）
//     * <p>
//     * 1. 创建excel对应的实体对象 参照{@link }
//     * <p>
//     * 2. 设置返回的 参数
//     * <p>
//     * 3. 直接写，这里注意，finish的时候会自动关闭OutputStream,当然你外面再关闭流问题不大
//     */
//    @GetMapping("/download")
//    public void download(HttpServletResponse response) throws IOException {
//
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//
//        List<Dish> dishes = dishMapper.selectList(null);
//        // 这里 指定文件
//        try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
//            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
//            for (int i = 0; i < 5; i++) {
//                EasyExcel.write(response.getOutputStream(), Dish.class)
//                        .sheet(i,"模板" + i)
//                        .doWrite(dishes);
//            }
//        }
//    }


}
