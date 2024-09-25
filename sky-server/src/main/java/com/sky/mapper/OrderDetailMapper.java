package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
* @author 凯祥
* @description 针对表【order_detail(订单明细表)】的数据库操作Mapper
* @createDate 2024-08-28 15:55:30
* @Entity generator.domain.OrderDetail
*/
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    List<OrderDetail> top10(LocalDate begin, LocalDate end);
}




