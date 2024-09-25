package com.sky.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
* @author 凯祥
* @description 针对表【orders(订单表)】的数据库操作Service
* @createDate 2024-08-28 15:54:50
*/
public interface OrdersService extends IService<Orders> {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;
    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);

    /**
     * 接单
     * @param ordersDTO
     */
    void confirm(OrdersDTO ordersDTO);

    /**
     * 取消订单
     * @param
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成
     * @param id
     */
    void complete(Long id);

    /**
     * 统计订单数据
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
}
