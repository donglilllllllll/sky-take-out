//package com.sky.controller.admin;
//
//import com.alibaba.excel.EasyExcel;
//import com.alibaba.excel.ExcelWriter;
//import com.alibaba.excel.util.ListUtils;
//import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
//import com.alibaba.excel.write.metadata.WriteSheet;
//import com.sky.entity.Dish;
//import com.sky.vo.DemoData;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.catalina.connector.Response;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.util.Date;
//import java.util.List;
//
///**
// * @Author：zhangkaixiang
// * @Package：com.sky.controller.admin
// * @Project：sky-take-out
// * @name：TestController
// * @Date：2024/7/6 21:23
// * @Filename：TestController
// */
//
//@RestController
//@Slf4j
//public class TestController {
//    /**
//     * 文件下载（失败了会返回一个有部分数据的Excel）
//     * <p>
//     * 1. 创建excel对应的实体对象 参照{@link DemoData}
//     * <p>
//     * 2. 设置返回的 参数
//     * <p>
//     * 3. 直接写，这里注意，finish的时候会自动关闭OutputStream,当然你外面再关闭流问题不大
//     */
//    @GetMapping("/admin/download")
//    public void download(HttpServletResponse response) throws IOException {
//        log.info("文件下载成功=====");
//
//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        String fileName = URLEncoder.encode("测试", "UTF-8")
//                .replaceAll("\\+", "%20");
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//
//        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
//        List<DemoData> data = data();
//        WriteSheet e1 = EasyExcel.writerSheet(0, "模板1").head(DemoData.class).build();
//
//        excelWriter.write(data,e1);
//        WriteSheet e2 = EasyExcel.writerSheet(1, "模板2").head(DemoData.class).build();
//        excelWriter.write(data,e2);
//        excelWriter.finish();
//
//
//    }
//
//    private List<DemoData> data() {
//        List<DemoData> list = ListUtils.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            DemoData data = new DemoData();
//            data.setString("字符串" + i);
//            data.setDate(new Date());
//            data.setDoubleData(0.56);
//            list.add(data);
//        }
//        return list;
//    }
//}
