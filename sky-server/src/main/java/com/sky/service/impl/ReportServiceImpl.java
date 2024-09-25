package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();//存放日期begin-end的日期
        List<BigDecimal> turnoverList = new ArrayList<>();//存放营业额
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//循环天数+1
            dateList.add(begin);
        }
        for (LocalDate date : dateList) {
            //根据日期查询营业额,状态为已完成
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//date日期的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//date日期的结束时间
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = ordersMapper.sumByMap(map);
            // 检查 turnover 是否为 null
            if (turnover == null) {
                turnover = 0.0; // 如果 turnover 为 null，则默认设置为 0.0
            }
            BigDecimal bigDecimal = new BigDecimal(turnover).setScale(2, RoundingMode.HALF_UP);
            turnoverList.add(bigDecimal);
        }
        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));//存放日期
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));//存放营业额
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();//存放日期begin-end的日期
        List<Integer> totalUserList = new ArrayList<>();//用户总量
        List<Integer> newUserList = new ArrayList<>();//新增用户
        UserReportVO userReportVO = new UserReportVO();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//循环天数+1
            dateList.add(begin);
        }
        for (LocalDate date : dateList) {
            //根据日期查询用户总数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//date日期的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//date日期的结束时间
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            //总用户数量（查询到截止日期的总用户数量）
            Integer totalUser = userMapper.countUserByMap(map);
            totalUserList.add(totalUser);
            //根据（end-begin日期)查询新增用户
            map.put("begin", beginTime);
            Integer newUser = userMapper.countUserByMap(map);
            newUserList.add(newUser);
        }
        userReportVO.setDateList(StringUtils.join(dateList, ","));//存放日期
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));//存放用户总量
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));//存放新增用户
        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();//存放日期begin-end的日期
        List<Integer> orderCountList = new ArrayList<>();//订单总数量
        List<Integer> validOrderCountList = new ArrayList<>();//有效订单数量
        OrderReportVO orderReportVO = new OrderReportVO();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//循环天数+1
            dateList.add(begin);
        }
        //查询每日有效的订单数量、订单数量
        for (LocalDate date : dateList) {
            //根据日期查询订单总数量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//date日期的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//date日期的结束时间
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Orders orderCount = ordersMapper.countByMap(map);
            orderCountList.add(orderCount.getOrderCounts() == null ? 0 : orderCount.getOrderCounts());//每日订单数
            validOrderCountList.add( orderCount.getValidOrderCounts() == null ? 0 : orderCount.getValidOrderCounts());//每日有效订单数
        }
        //订单总数量、有效订单总数量
        ;
        orderReportVO.setTotalOrderCount(orderCountList.stream().reduce(0, Integer::sum));//订单总数量
        orderReportVO.setValidOrderCount(validOrderCountList.stream().reduce(0, Integer::sum));//有效订单总数量
        orderReportVO.setDateList(StringUtils.join(dateList, ","));//存放日期
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList, ","));//存放订单总数量
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));//存放有效订单数量
        //计算订单完成率
        if (orderReportVO.getTotalOrderCount() > 0) {
            orderReportVO.setOrderCompletionRate(new BigDecimal(orderReportVO.getValidOrderCount()).divide(new BigDecimal(orderReportVO.getTotalOrderCount()), 2, RoundingMode.HALF_UP).doubleValue());
        } else {
            orderReportVO.setOrderCompletionRate(0.0);
        }

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //查询销量排名前十的菜品或套餐
        List<OrderDetail> orderDetails = orderDetailMapper.top10(begin, end);
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        if (orderDetails != null && orderDetails.size() > 0) {
            List<String> nameList = orderDetails.stream().map(OrderDetail::getName).collect(Collectors.toList());
            List<Integer> numberList = orderDetails.stream().map(OrderDetail::getTotalNumber).collect(Collectors.toList());
            salesTop10ReportVO.setNameList(StringUtils.join(nameList, ","));
            salesTop10ReportVO.setNumberList(StringUtils.join(numberList, ","));
        }
        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库，获取营业数据，查询近30天的数据
        LocalDateTime begin = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);//查询概览数据

        //2.通过poi将数据写入到Excel文件
        //类加载器去读取模板文件
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            if (ins != null) {
                //基于模板创建Excel文件
                XSSFWorkbook excel = new XSSFWorkbook(ins);//插件一个Excel文件
                XSSFSheet sheet = excel.getSheet("Sheet1");//获取sheet页
                //填充时间
                sheet.getRow(1).getCell(1).setCellValue("时间：" + begin.toLocalDate() + " - " + end.toLocalDate());
                //获取第四行
                XSSFRow row4 = sheet.getRow(3);
                row4.getCell(2).setCellValue(businessDataVO.getTurnover());//填充营业额
                row4.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());//填充订单完成率
                row4.getCell(6).setCellValue(businessDataVO.getNewUsers());//填充新增用户数量
                //获取第五行
                XSSFRow row5 = sheet.getRow(4);
                row5.getCell(2).setCellValue(businessDataVO.getValidOrderCount());//填充有效订单数量
                row5.getCell(4).setCellValue(businessDataVO.getUnitPrice());//填充平均客单价

                LocalDate localDate = LocalDate.now().minusDays(30);
                for (int i = 0; i < 30; i++) {
                    localDate = localDate.plusDays(1);
                    BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN),
                            LocalDateTime.of(localDate, LocalTime.MAX));//查询某一天数据
                    XSSFRow row = sheet.getRow(7 + i);
                    row.getCell(1).setCellValue(localDate.toString());//填充日期
                    row.getCell(2).setCellValue(businessData.getTurnover());//填充营业额
                    row.getCell(3).setCellValue(businessData.getValidOrderCount());//填充有效订单数量
                    row.getCell(4).setCellValue(businessData.getOrderCompletionRate());//填充订单完成率
                    row.getCell(5).setCellValue(businessData.getUnitPrice());//填充平均客单价
                    row.getCell(6).setCellValue(businessData.getNewUsers());//填充新增用户数量

                }

                //3.通过输出流将Excel文件下载到客户端浏览器
                OutputStream out = response.getOutputStream();
                excel.write(out);

                //关闭资源
                out.close();
                excel.close();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
