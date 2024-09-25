package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单{}", LocalDateTime.now());
        //查询下单时间超过15分钟的订单
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT) //订单状态为代付款
                .le(Orders::getOrderTime, LocalDateTime.now().minusMinutes(15))//订单时间在当前时间之前15分钟
        );
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                ordersMapper.update(Orders.builder()
                                .status(Orders.CANCELLED)
                                .cancelReason("订单超时，已取消")
                                .cancelTime(LocalDateTime.now())
                                .build(),
                        new LambdaQueryWrapper<Orders>()
                                .eq(Orders::getId, order.getId())
                                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                );
            }
        }


    }

    @Scheduled(cron = "0 0 1 * * ? ")//每天凌晨1点触发
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单{}", LocalDateTime.now());
        //查询处于派送中的订单
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS) //订单状态为派送中
                .le(Orders::getEstimatedDeliveryTime, LocalDateTime.now().minusHours(1))//处理上一个工作日的派送中订单
        );
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                ordersMapper.update(Orders.builder()
                                .status(Orders.COMPLETED)
                                .deliveryTime(LocalDateTime.now())
                                .build(),
                        new LambdaQueryWrapper<Orders>()
                                .eq(Orders::getId, order.getId())
                                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                );
            }
        }
    }
}
