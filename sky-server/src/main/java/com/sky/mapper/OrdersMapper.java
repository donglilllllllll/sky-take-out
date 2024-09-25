package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Map;

/**
* @author 凯祥
* @description 针对表【orders(订单表)】的数据库操作Mapper
* @createDate 2024-08-28 15:54:50
* @Entity generator.domain.Orders
*/
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    OrderStatisticsVO statistics();


    Double sumByMap(Map<String, Object> map);


    Orders countByMap(Map<String, Object> map);

    Integer countByMap1(Map<String,Object> map);
}




