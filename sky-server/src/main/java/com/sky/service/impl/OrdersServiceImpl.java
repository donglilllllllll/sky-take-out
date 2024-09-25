package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 凯祥
 * @description 针对表【orders(订单表)】的数据库操作Service实现
 * @createDate 2024-08-28 15:54:50
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //校验数据，处理各种业务异常（地址簿，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //获取当前登录人Id
        Long currentId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, currentId));
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setPayStatus(Orders.UN_PAID);//订单状态，1待付款
        orders.setStatus(Orders.PENDING_PAYMENT);//订单状态，0待派送，1已派送，2已完成，3已取消
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//订单号
        orders.setPhone(addressBook.getPhone());//手机号
        orders.setConsignee(addressBook.getConsignee());//s签收人
        orders.setUserId(currentId);
        ordersMapper.insert(orders);
        //向订单明细表插入多条数据
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());//当前订单明细关联的订单Id
            orderDetailMapper.insert(orderDetail);
        }
        //删除购物车数据
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, currentId));
        //返回订单数据（构建）
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = new JSONObject();
//        jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal("0.01"), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        //跳过微信支付接口，伪造生成预支付交易单 伪造json返回数据
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, ordersPaymentDTO.getOrderNumber()));
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.updateById(orders);
        //通过WebSocketServer向客户端推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);//1标识来单提醒，2标识客户催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + ordersPaymentDTO.getOrderNumber());
        String jsonString = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, outTradeNo));
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);
        //通过WebSocketServer向客户端推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);//1标识来单提醒，2标识客户催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo);
        String jsonString = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    public PageResult<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult<OrderVO> pageResult = new PageResult<>();
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersMapper.selectPage(page, new LambdaQueryWrapper<Orders>()
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())//订单状态
                .like(ordersPageQueryDTO.getNumber() != null, Orders::getNumber, ordersPageQueryDTO.getNumber())//订单号
                .like(ordersPageQueryDTO.getPhone() != null, Orders::getPhone, ordersPageQueryDTO.getPhone())//手机号
                .between(ordersPageQueryDTO.getBeginTime() != null && ordersPageQueryDTO.getEndTime() != null,
                        Orders::getOrderTime, ordersPageQueryDTO.getBeginTime(), ordersPageQueryDTO.getEndTime())//下单时间
        );
        List<OrderVO> records = new ArrayList<>();
        page.getRecords().forEach(item -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(item, orderVO);
            AddressBook address = addressBookMapper.getById(item.getAddressBookId());
            orderVO.setAddress(address.getProvinceName()+address.getCityName()+address.getDistrictName()+address.getDetail());
            records.add(orderVO);
        });
        pageResult.setRecords(records);//总数据集合
        pageResult.setTotal(page.getTotal());//总条数
        return pageResult;
    }

    @Override
    @Transactional
    public OrderVO details(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders orders = ordersMapper.selectById(id);
        if (orders != null) {
            BeanUtils.copyProperties(orders, orderVO);
        }
        //查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, id));
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void reminder(Long id) {
        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.selectById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //通过WebSocketServer向客户端推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);//1标识来单提醒，2标识客户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + ordersDB.getNumber());
        String jsonString = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    public void confirm(OrdersDTO ordersDTO) {
        //接单修改orders表的状态为已接单
        Orders orders = Orders.builder()
                .id(ordersDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.updateById(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //取消订单
        Orders orders = ordersMapper.selectById(ordersCancelDTO.getId());
        //判断订单是否为待派单状态，如果不是，则不能取消订单
        if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单状态为已取消
        Orders order = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        ordersMapper.updateById(order);


    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //拒单修改orders表的状态为已取消
        Orders orders = ordersMapper.selectById(ordersRejectionDTO.getId());
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .build();
        ordersMapper.updateById(order);
    }

    @Override
    public void delivery(Long id) {
        //订单派送修改订单状态为派送中
        Orders orders = ordersMapper.selectById(id);
        if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        ordersMapper.updateById(order);
    }

    @Override
    public void complete(Long id) {
        //订单完成修改订单状态为已完成
        Orders orders = ordersMapper.selectById(id);
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        ordersMapper.updateById(order);
    }

    @Override
    public OrderStatisticsVO statistics() {
        return ordersMapper.statistics();
    }

    @Override
    public PageResult<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //查询历史订单
        PageResult<OrderVO> pageResult = new PageResult<>();
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersMapper.selectPage(page, new LambdaQueryWrapper<Orders>()
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())//订单状态
        );
        List<OrderVO> records = new ArrayList<>();
        page.getRecords().forEach(item -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(item, orderVO);
            //查询订单明细
            List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getOrderId, item.getId()));
            orderVO.setOrderDetailList(orderDetailList);
            records.add(orderVO);
        });

        pageResult.setRecords(records);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

}




