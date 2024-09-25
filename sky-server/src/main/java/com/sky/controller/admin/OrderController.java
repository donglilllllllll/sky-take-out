package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.controller.user
 * @Project：sky-take-out
 * @name：OrderController
 * @Date：2024/8/28 14:52
 * @Filename：OrderController
 */

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult<OrderVO>> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageResult<OrderVO> pageResult = ordersService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrderVO orderVO = ordersService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersDTO ordersDTO) {
        log.info("接单：{}", ordersDTO);
        ordersService.confirm(ordersDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单：{}", ordersCancelDTO);
        ordersService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 订单拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("订单拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("订单拒单：{}", ordersRejectionDTO);
        ordersService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 订单派送
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("订单派送")
    public Result delivery(@PathVariable Long id) {
        log.info("订单派送：{}", id);
        ordersService.delivery(id);
        return Result.success();
    }

    /**
     * 订单完成
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("订单完成")
    public Result complete(@PathVariable Long id) {
        log.info("订单完成：{}", id);
        ordersService.complete(id);
        return Result.success();
    }
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计开始====");
        return Result.success(ordersService.statistics());
    }


}
