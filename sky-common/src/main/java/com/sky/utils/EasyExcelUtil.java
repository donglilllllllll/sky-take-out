//package com.sky.utils;
//
//import com.alibaba.excel.EasyExcel;
//import com.alibaba.excel.ExcelWriter;
//import com.alibaba.excel.annotation.ExcelProperty;
//import com.alibaba.excel.converters.AutoConverter;
//import com.alibaba.excel.converters.Converter;
//import com.alibaba.excel.converters.longconverter.LongStringConverter;
//import com.alibaba.excel.enums.WriteDirectionEnum;
//import com.alibaba.excel.metadata.Head;
//import com.alibaba.excel.metadata.data.ReadCellData;
//import com.alibaba.excel.metadata.data.WriteCellData;
//import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
//import com.alibaba.excel.write.handler.CellWriteHandler;
//import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
//import com.alibaba.excel.write.merge.AbstractMergeStrategy;
//import com.alibaba.excel.write.metadata.WriteSheet;
//import com.alibaba.excel.write.metadata.fill.FillConfig;
//import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
//import com.alibaba.excel.write.metadata.style.WriteCellStyle;
//import com.alibaba.excel.write.metadata.style.WriteFont;
//import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
//import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
//import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
//import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
//import com.eigpay.sp.mobilegw.consts.FileExportConstant;
//import com.eigpay.sp.mobilegw.util.FileUtil;
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//import com.google.common.collect.Maps;
//import com.riped.online.infra.shared.excel.converters.LocalDateNumberConverter;
//import com.riped.online.infra.shared.excel.converters.LocalDateStringConverter;
//import com.sun.rowset.internal.Row;
//import javafx.scene.control.Cell;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.compress.utils.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.logging.log4j.util.Strings;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.util.CellRangeAddress;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.PostConstruct;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.math.BigInteger;
//import java.net.URLEncoder;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
///**
// * EasyExcel  导入导出工具
// *
// * @author zk
// * @date 2021/12/30 17:07
// **/
//@Slf4j
//@Component
//public class EasyExcelUtil {
//
//    /**
//     * 文件保存目录
//     */
//    private static String baseFolder;
//
//    @Value("${file.baseFolder:/home/overlord/OPTG}")
//    private String folder;
//
//    @PostConstruct
//    public void init() {
//        EasyExcelUtil.baseFolder = folder;
//    }
//
//    private static final String ORDER_BY = "create_time desc";
//
//    /**
//     * Excel 文件导入
//     *
//     * @param clazz        导入对象
//     * @param saveConsumer 数据批量保存
//     * @param <T>
//     * @return
//     */
//    public static <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<List<T>> saveConsumer) {
//        importExcel(inputStream, clazz, null, saveConsumer);
//    }
//
//    /**
//     * excel 文件导入
//     *
//     * @param inputStream   输入文件流
//     * @param clazz         导入对象
//     * @param checkConsumer 单条数据校验
//     * @param saveConsumer  数据批量保存
//     * @param <T>
//     * @return
//     */
//    public static <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<T> checkConsumer, Consumer<List<T>> saveConsumer) {
//        EasyExcel.read(inputStream, clazz, new DefaultAnalysisEventListener<>(checkConsumer, saveConsumer)).registerConverter(new LocalDateStringConverter()).registerConverter(new LocalDateNumberConverter()).sheet().doRead();
//    }
//
//    /**
//     * excel 自动填充工厂号
//     *
//     * @param inputStream
//     * @param clazz
//     * @param checkConsumer
//     * @param saveConsumer
//     * @param autoTenantId  是否自动填充工厂号
//     * @param <T>
//     * @return
//     */
//    public static <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<T> checkConsumer, Consumer<List<T>> saveConsumer, boolean autoTenantId) {
//        EasyExcel.read(inputStream, clazz, new DefaultAnalysisEventListener<>(checkConsumer, saveConsumer)).registerConverter(new LocalDateStringConverter()).registerConverter(new LocalDateNumberConverter()).sheet().doRead();
//    }
//
//    /**
//     * 下载Resources目录下的模文件
//     *
//     * @param response
//     * @param filePath
//     * @param fileName
//     * @return void
//     * @author zk
//     * @date 2022/01/19 14:25
//     */
//    public static void downloadTemplate(HttpServletResponse response, String filePath, String fileName) throws IOException {
//        try (ServletOutputStream outputStream = response.getOutputStream()) {
//            InputStream inputStream = EasyExcelUtil.class.getClassLoader().getResourceAsStream(filePath);
//            if (inputStream == null) {
//                /** TODO 抛出异常没找到对应文件*/
//            }
//            setResponseHeader(response, fileName);
//            IOUtils.copy(inputStream, outputStream);
//        }
//    }
//
//    /**
//     * 导出到文件
//     *
//     * @param clazz          表头对象
//     * @param selectFunction 查询方法
//     * @param convert        转换方法
//     * @param orderBy        排序
//     * @param relativePath   相对路径
//     * @param fileName       文件名不带.xlsx
//     * @param <R>
//     * @return
//     */
//    public static <T, R> Map<String, String> exportExcel(Class<R> clazz, Supplier<List<T>> selectFunction, Function<List<T>, List<R>> convert, String orderBy, String relativePath, String fileName) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            exportExcel(outputStream, clazz, fileName, selectFunction, convert, StringUtils.isBlank(orderBy) ? ORDER_BY : orderBy);
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * @param clazz    导出的vo
//     * @param list     内容
//     * @param fileName 文件名,相对路径默认"/ONLINE/XM/TMP/", 后缀默认yyyyMMdd
//     * @return 文件路径
//     */
//    public static <T> Map<String, String> exportExcel(Class<T> clazz, List<T> list, String fileName) {
//        String fullPath = buildFullPath("/ONLINE/XM/TMP/", fileName + new SimpleDateFormat("yyyyMMdd").format(new Date()));
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz).build()) {
//            WriteSheet writeSheet = EasyExcel.writerSheet(fileName).registerConverter(new LongStringConverter()).registerConverter(new LocalDateStringConverter()).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
//            excelWriter.write(list, writeSheet);
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//    public static <T> Map<String, String> exportExcel(Map<String,String> headMap,List<T> list, String fileName) {
//        String fullPath = buildFullPath("/ONLINE/XM/TMP/", fileName + new SimpleDateFormat("yyyyMMdd").format(new Date()));
//        if(headMap.isEmpty()){
//            throw new RuntimeException("导出表头参数为空！");
//        }
//        List<List<Object>> exportDataList=new ArrayList<>();
//        for (Object obj:list){
//                List<Object> value=getValueByName(obj,headMap);
//                exportDataList.add(value);
//        }
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
//            WriteSheet writeSheet = EasyExcel.writerSheet(fileName).head(getHead(headMap)).registerConverter(new LongStringConverter()).registerConverter(new LocalDateStringConverter()).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
//            excelWriter.write(exportDataList, writeSheet);
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//    public static List<List<String>>getHead(Map<String,String> headMap){
//        List<List<String>> headList = new ArrayList<>();
//        Set<String> mapKey=headMap.keySet();
//        for (String attributeName:mapKey){
//            List<String> itemList = new ArrayList<>();
//            itemList.add(headMap.get(attributeName));
//            headList.add(itemList);
//        }
//        return headList;
//    }
//    public static List<Object> getValueByName(Object obj,Map<String,String> headMap){
//        Set<String> mapKey=headMap.keySet();
//        try {
//            List<Object> itemList = new ArrayList<>();
//            Object newObj=obj.getClass().newInstance();
//            for (String attributeName:mapKey){
//                Object objvalue=getFieldValue(obj,attributeName);
//                setFieldValue(newObj,attributeName,objvalue);
//                itemList.add(objvalue);
//            }
//            return itemList;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
//        Class<?> clazz = obj.getClass();
//        Field field = clazz.getDeclaredField(fieldName);
//
//        field.setAccessible(true);
//        ExcelProperty excelProperty =field.getAnnotation(ExcelProperty.class);
//        if (null!=excelProperty){
//            Class<? extends Converter<?>> convertClazz = excelProperty.converter();
//            if (null!=convertClazz && AutoConverter.class!=convertClazz){
//                Converter<?> converter = convertClazz.getDeclaredConstructor().newInstance();
//                Class<?> parentClass = convertClazz.getSuperclass().getSuperclass();
//                Method method= parentClass.getMethod("getDicName",String.class);
//                Object fieldValue=field.get(obj);
//                return method.invoke(converter,fieldValue.toString());
//            }
//        }
//        return field.get(obj);
//    }
//    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
//        Class<?> clazz = obj.getClass();
//        Field field = clazz.getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(obj, value);
//    }
//    /**
//     * 拼接全路径
//     *
//     * @param relativePath
//     * @return
//     */
//    public static String buildFullPath(String relativePath, String fileName) {
//        relativePath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
//        return baseFolder + (relativePath.endsWith("/") ? relativePath : relativePath + "/") + FileUtil.filenameAddSeq(fileName) + ".xlsx";
//    }
//
//    /**
//     * excel 文件导出
//     *
//     * @param outputStream
//     * @param clazz
//     * @param fileName
//     * @param selectFunction
//     * @param convert
//     * @param orderBy
//     * @param <R>
//     * @return
//     */
//    private static <T, R> void exportExcel(OutputStream outputStream, Class<R> clazz, String fileName, Supplier<List<T>> selectFunction, Function<List<T>, List<R>> convert, String orderBy) {
//        try (ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz).build()) {
//            /**
//             * 1.Long 转String
//             * 2.Bigdecimal 转String
//             * 3.自适应宽度
//             * */
//            WriteSheet writeSheet = EasyExcel.writerSheet(fileName)
//                    .registerConverter(new LongStringConverter()).registerConverter(new LocalDateStringConverter()).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
//            PageInfo<T> page;
//            int pageNo = 1;
//            do {
//                PageHelper.startPage(pageNo, 2000, orderBy);
//                List<T> pageList = selectFunction.get();
//                page = new PageInfo<>(pageList);
//                List<R> list = convert.apply(page.getList());
//                excelWriter.write(list, writeSheet);
//                pageNo++;
//            } while (page.isHasNextPage());
//        }
//    }
//
//    /**
//     * description: 导出空模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头类
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xlsx
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, Class<?> head, String relativePath, String fileName) {
//        return exportExcel(sheetNames, head, null, relativePath, fileName);
//    }
//
//
//    /**
//     * description: 导出空模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头类
//     * @param dataList     数据集合
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xlsx
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static <T> Map<String, String> exportExcel(List<String> sheetNames, Class<T> head, List<T> dataList, String relativePath, String fileName, CellWriteHandler... cellWriteHandlers) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter write = EasyExcel.write(outputStream).build()) {
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            sheetNames.forEach(line -> {
//                ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).registerConverter(new LocalDateStringConverter())
//                        .registerWriteHandler(exportStyle())
//                        .head(head);
//                for (CellWriteHandler cellWriteHandler : cellWriteHandlers) {
//                    writerSheetBuilder.registerWriteHandler(cellWriteHandler);
//                }
//                write.write(dataList, writerSheetBuilder.build());
//            });
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    /**
//     * description: 导出空模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, List<List<String>> head, String relativePath, String fileName) {
//        return exportExcel(sheetNames, head, null, relativePath, fileName);
//    }
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, List<List<String>> head, List<List<Object>> data, String relativePath, String fileName) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter write = EasyExcel.write(outputStream).build();
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            sheetNames.forEach(line -> {
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head).registerConverter(new LocalDateStringConverter())
//                        .registerWriteHandler(exportStyle()).build();
//                write.write(data, sheet);
//            });
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, Map<String, List<List<String>>> head, List<List<Object>> data, String relativePath, String fileName) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter write = EasyExcel.write(outputStream).build();
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            sheetNames.forEach(line -> {
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head.get(line)).registerConverter(new LocalDateStringConverter())
//                        .registerWriteHandler(exportStyle()).build();
//                write.write(data, sheet);
//            });
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, Map<String, List<List<String>>> head, Map<String, List<List<Object>>> data, String relativePath, String fileName) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter write = EasyExcel.write(outputStream).build();
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            sheetNames.forEach(line -> {
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head.get(line)).registerConverter(new LocalDateStringConverter())
//                        .registerWriteHandler(exportStyle()).build();
//                write.write(data.get(line), sheet);
//            });
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetName    sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(String sheetName, List<List<String>> head, List<List<Object>> data, String relativePath, String fileName, CellWriteHandler... cellWriteHandlers) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter write = EasyExcel.write(outputStream).build();
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            ExcelWriterSheetBuilder sheetBuilder = EasyExcel.writerSheet(sheetNo.getAndIncrement(), sheetName).head(head).registerConverter(new LocalDateStringConverter());
//            for (CellWriteHandler cellWriteHandler : cellWriteHandlers) {
//                sheetBuilder.registerWriteHandler(cellWriteHandler);
//            }
//            if (cellWriteHandlers.length == 0) {
//                sheetBuilder.registerWriteHandler(exportStyle());
//            }
//            WriteSheet sheet = sheetBuilder.build();
//            write.write(data, sheet);
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, List<List<String>> head, Map<String, List<List<Object>>> dataMap, String relativePath, String fileName, CellWriteHandler... cellWriteHandlers) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter write = EasyExcel.write(outputStream).build();
//            for (String sheetName : sheetNames) {
//                ExcelWriterSheetBuilder sheetBuilder = EasyExcel.writerSheet(sheetName).head(head).registerConverter(new LocalDateStringConverter());
//                for (CellWriteHandler cellWriteHandler : cellWriteHandlers) {
//                    sheetBuilder.registerWriteHandler(cellWriteHandler);
//                }
//                WriteSheet sheet = sheetBuilder.registerWriteHandler(exportStyle()).build();
//                write.write(dataMap.get(sheetName), sheet);
//            }
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    public static Map<String, String> exportFillExcel(List<?> data, String relativePath, String fileName, String templateFilePath) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(EasyExcelUtil.class.getClassLoader().getResourceAsStream(templateFilePath)).build();
//            WriteSheet writeSheet = EasyExcel.writerSheet().build();
//            // 放入的是list列表中的数据
//            excelWriter.fill(data, writeSheet);
//            excelWriter.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * description: 横向填充
//     *
//     * @param data
//     * @param relativePath
//     * @param fileName
//     * @param templateFilePath
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportHorizontalFillExcel(List<?> data, String relativePath, String fileName, String templateFilePath, CellWriteHandler... cellWriteHandler) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(EasyExcelUtil.class.getClassLoader().getResourceAsStream(templateFilePath)).build();
//            ExcelWriterSheetBuilder excelWriterSheetBuilder = EasyExcel.writerSheet().sheetNo(0);
//            for (CellWriteHandler writeHandler : cellWriteHandler) {
//                excelWriterSheetBuilder.registerWriteHandler(writeHandler);
//            }
//            WriteSheet writeSheet = excelWriterSheetBuilder.build();
//            FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
//            // 放入的是list列表中的数据
//            excelWriter.fill(data, fillConfig, writeSheet);
//            excelWriter.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param dataMap      表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(LinkedList<String> sheetNames, Map<String, Object> dataMap, List<Object> data, List<Object> totalData,
//                                                  String relativePath, String fileName, String templateFilePath, CellWriteHandler cellWriteHandler) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath)) {
//            // ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(EasyExcelUtil.class.getClassLoader().getResourceAsStream(templateFilePath))
//            //         .registerWriteHandler(cellWriteHandler).build();
//            ExcelWriter write = EasyExcel.write(outputStream).withTemplate(EasyExcelUtil.class.getClassLoader().getResourceAsStream(templateFilePath)).build();
//            // AtomicInteger sheetNo = new AtomicInteger(0);
//
//            for (int i = 0; i < sheetNames.size(); i++) {
//                // WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head)
//                //         .build();
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNames.get(i)).build();
//                // 放入的是list列表中的数据
//                write.fill(data.get(i), sheet);
//                // map中的数据
//                write.fill(dataMap, sheet);
//                write.fill(totalData.get(i), sheet);
//            }
//            // write.write(data, sheet);
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    /**
//     * 导出Excel文件
//     *
//     * @param sheetNames sheet页名称
//     * @param head       表头集合
//     * @param data       数据值
//     * @return 文件路径和文件名称，包含文件扩展名
//     */
//    public static Map<String, String> exportExcelWork(List<String> sheetNames, List<List<String>> head, List<List<Object>> data, String relativePath, String fileName) {
//        // 处理数据
//        // 导出Excel文件
//        String fullPath = buildFullPath(relativePath, fileName);
//        Map<Integer, Float> rowHeight = new HashMap<>();
//        Map<Integer, Integer> columnWidth = new HashMap<>();
//        try (OutputStream outputStream = FileUtil.writeFile(buildFullPath(fullPath, fileName));
//             ExcelWriter writer = EasyExcel.write(outputStream).build()) {
//            AtomicInteger sheetNo = new AtomicInteger();
//            sheetNames.forEach(sheetName -> {
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), sheetName)
//                        .head(head).registerWriteHandler(new AuditUnitRowHeightRowHandler(rowHeight))
//                        .registerWriteHandler(new AuditUnitColumnWidthHandler(columnWidth)).build();
//                writer.write(data, sheet);
//            });
//            writer.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//
//        Map<String, String> result = new HashMap<>();
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    // /**
//    //  * 处理数据
//    //  *
//    //  * @param data 原始数据
//    //  * @return 处理后的数据
//    //  */
//    // private static List<List<Object>> processData(List<Map<String, Object>> data) {
//    //     List<List<Object>> processedData = Lists.newArrayList();
//    //     for (Map<String, Object> map : data) {
//    //         Object yearObj = map.get("year");
//    //         Object sumWorkingTimeObj = map.get("sumWorkingTime");
//    //         Object keWorkingTimeObj = map.get("keWorkingTime");
//    //         Object pmStaffTimesListObj = map.get("pmStaffTimesList");
//    //
//    //         int rows = 1;
//    //         if (pmStaffTimesListObj != null) {
//    //             List<Map<String, Object>> pmStaffTimesList = (List<Map<String, Object>>) pmStaffTimesListObj;
//    //             rows += pmStaffTimesList.size();
//    //         }
//    //
//    //         List<Object> row = Lists.newArrayListWithCapacity(rows);
//    //         boolean isFirstRow = true;
//    //         if (pmStaffTimesListObj != null) {
//    //             List<Map<String, PmProjectStaffTimesInputBO>> pmStaffTimesList = (List<Map<String, PmProjectStaffTimesInputBO>>) pmStaffTimesListObj;
//    //             for (Map<String, PmProjectStaffTimesInputBO> pmStaffTimes : pmStaffTimesList) {
//    //                 if (isFirstRow) {
//    //                     // 第一行
//    //                     row.add(yearObj);
//    //                     row.add(sumWorkingTimeObj);
//    //                     row.add(keWorkingTimeObj);
//    //                     row.addAll(pmStaffTimes.values().stream().map(PmProjectStaffTimesInputBO::toString).collect(Collectors.toList()));
//    //                     processedData.add(row);
//    //                     isFirstRow = false;
//    //                 } else {
//    //                     // 要扩展行
//    //                     List<Object> newRow = new ArrayList<>();
//    //                     newRow.add(StringUtils.EMPTY); // year列
//    //                     newRow.add(StringUtils.EMPTY); // sumWorkingTime列
//    //                     newRow.add(StringUtils.EMPTY); // keWorkingTime列
//    //                     newRow.addAll(pmStaffTimes.values().stream().map(PmProjectStaffTimesInputBO::toString).collect(Collectors.toList()));
//    //                     processedData.add(newRow);
//    //                 }
//    //             }
//    //         } else {
//    //             // 没有pmStaffTimesList数据
//    //             row.add(yearObj);
//    //             row.add(sumWorkingTimeObj);
//    //             row.add(keWorkingTimeObj);
//    //             processedData.add(row);
//    //         }
//    //     }
//    //     return processedData;
//    // }
//
//
//    /**
//     * description: 导出带数据模板
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static <T> Map<String, String> exportExcel2(List<String> sheetNames, Class<T> head, List<T> data, String fileName, List<List<String>> mergeList) {
//        String fullPath = buildFullPath("/ONLINE/XM/TMP/", fileName + new SimpleDateFormat("yyyyMMdd").format(new Date()));
//
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter write = EasyExcel.write(outputStream).build()) {
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            Map<Integer, Float> rowHeight = new HashMap<>();
//            Map<Integer, Integer> columnWidth = new HashMap<>();
//            sheetNames.forEach(line -> {
//                ExcelWriterSheetBuilder excelWriterSheetBuilder = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head).needHead(Boolean.TRUE)
//                        .registerConverter(new LocalDateStringConverter()).registerWriteHandler(exportStyle())
//                        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).registerWriteHandler(new AuditUnitRowHeightRowHandler(rowHeight));
//                for (int i = 0; i < mergeList.size(); i++) {
//                    excelWriterSheetBuilder.registerWriteHandler(new CustomMergeStrategy(mergeList.get(i), i));
//                }
//                WriteSheet sheet = excelWriterSheetBuilder.build();
//                write.write(data, sheet);
//
//            });
//
//
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    /**
//     * description: 人员工时导出
//     *
//     * @param sheetNames   sheet页名称
//     * @param head         表头集合
//     * @param data         数据值
//     * @param relativePath 相对路径
//     * @param fileName     文件名不带.xls
//     * @return java.util.Map<java.lang.String, java.lang.String>
//     */
//    public static Map<String, String> exportExcel(List<String> sheetNames, List<List<String>> head, List<List<Object>> data, String relativePath, String fileName, HashMap<Integer, Float> rowHeight, HashMap<Integer, Integer> columnWidth) {
//        String fullPath = buildFullPath(relativePath, fileName);
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter write = EasyExcel.write(outputStream).build()) {
//            AtomicInteger sheetNo = new AtomicInteger(0);
//            sheetNames.forEach(line -> {
//                WriteSheet sheet = EasyExcel.writerSheet(sheetNo.getAndIncrement(), line).head(head)
//                        .registerConverter(new LocalDateStringConverter()).registerWriteHandler(exportStyle())
//                        .registerWriteHandler(new AuditUnitColumnWidthHandler(columnWidth)).registerWriteHandler(new AuditUnitRowHeightRowHandler(rowHeight)).build();
//                write.write(data, sheet);
//
//            });
//            write.finish();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//        HashMap<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//    /**
//     * 设置下载请求头
//     *
//     * @param response
//     * @param fileName
//     * @return void
//     * @author zk
//     * @date 2022/01/19 14:24
//     */
//    private static void setResponseHeader(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
//    }
//
//
//    /**
//     * 获取单元格内容并转字符串
//     *
//     * @param readCellData
//     * @return
//     */
//    public static String getStringCellValue(ReadCellData readCellData) {
//        switch (readCellData.getType()) {
//            case NUMBER:
//                return readCellData.getNumberValue().toPlainString();
//            case BOOLEAN:
//                return readCellData.getBooleanValue().toString();
//            case STRING:
//            case ERROR:
//                return readCellData.getStringValue();
//            case EMPTY:
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * 获取单元格内容并转BigInteger
//     *
//     * @param readCellData
//     * @return
//     */
//    public static BigInteger getBigIntegerCellValue(ReadCellData readCellData) {
//        switch (readCellData.getType()) {
//            case NUMBER:
//                return readCellData.getNumberValue().toBigInteger();
//            case STRING:
//                return new BigInteger(readCellData.getStringValue());
//            case BOOLEAN:
//            case ERROR:
//            case EMPTY:
//            default:
//                return null;
//        }
//    }
//
//
//    public static class AuditUnitColumnWidthHandler extends AbstractColumnWidthStyleStrategy {
//        private final Map<Integer, Integer> columnIndexMap;
//
//        public AuditUnitColumnWidthHandler(Map<Integer, Integer> columnIndexMap) {
//            this.columnIndexMap = columnIndexMap;
//        }
//
//        @Override
//        protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer integer, Boolean isHead) {
//            if (columnIndexMap.containsKey(cell.getColumnIndex())) {
//                Sheet sheet = writeSheetHolder.getSheet();
//                int initLength = columnIndexMap.get(cell.getColumnIndex());
//                sheet.setColumnWidth(cell.getColumnIndex(), (initLength + (initLength / 4) * 5) * 256);
//            }
//        }
//    }
//
//
//    public static class AuditUnitRowHeightRowHandler extends AbstractRowHeightStyleStrategy {
//        private final Map<Integer, Float> rowHeight;
//
//        public AuditUnitRowHeightRowHandler(Map<Integer, Float> rowHeight) {
//            this.rowHeight = rowHeight;
//        }
//
//        @Override
//        protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
//            if (rowHeight.containsKey(relativeRowIndex)) {
//                row.setHeightInPoints(rowHeight.get(relativeRowIndex));
//            }
//        }
//
//        @Override
//        protected void setContentColumnHeight(Row row, int relativeRowIndex) {
//            if (rowHeight.containsKey(relativeRowIndex)) {
//                row.setHeightInPoints(rowHeight.get(relativeRowIndex));
//            }
//        }
//    }
//
//    private static HorizontalCellStyleStrategy exportStyle() {
//        WriteCellStyle contentWriteCellStyle = getContentWriteCellStyle();
//        // 设置头部样式
//        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
//        // 设置头部标题居中
//        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
//        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
//    }
//
//    @NotNull
//    private static WriteCellStyle getContentWriteCellStyle() {
//        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
//        // 细实线
//        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
//        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
//        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
//        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
//        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
//        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
//        contentWriteCellStyle.setWrapped(true);
//        WriteFont writeFont = new WriteFont();
//        writeFont.setFontName("宋体");
//        writeFont.setFontHeightInPoints((short) 12);
//        contentWriteCellStyle.setWriteFont(writeFont);
//        return contentWriteCellStyle;
//    }
//
//    public static HorizontalCellStyleStrategy exportBookMarkStyle() {
//        WriteCellStyle contentWriteCellStyle = getContentWriteCellStyle();
//        // 设置头部样式
//        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
//        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.index);
//        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
//        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        WriteFont headWriteFont = new WriteFont();
//        headWriteFont.setFontName("宋体");
//        headWriteFont.setFontHeightInPoints((short) 14);
//        headWriteFont.setBold(true);
//        headWriteCellStyle.setWriteFont(headWriteFont);
//        // 设置头部标题居中
//        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
//        headWriteCellStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
//    }
//
//    public static class CustomMergeStrategy extends AbstractMergeStrategy {
//        /**
//         * 分组，每几行合并一次
//         */
//        private List<Integer> exportFieldGroupCountList;
//        /**
//         * 目标合并列index
//         */
//        private Integer targetColumnIndex;
//
//        // 需要开始合并单元格的首行index
//        private Integer rowIndex;
//
//        // exportDataList为待合并目标列的值
//        public CustomMergeStrategy(List<String> exportDataList, Integer targetColumnIndex) {
//            this.exportFieldGroupCountList = getGroupCountList(exportDataList);
//            this.targetColumnIndex = targetColumnIndex;
//        }
//
//        // exportDataList为待合并目标列的值
//
//        /**
//         * description:
//         *
//         * @param exportDataList    列数据
//         * @param targetColumnIndex 目标列
//         * @param rowIndex          数据开始行
//         * @return
//         */
//        public CustomMergeStrategy(List<String> exportDataList, Integer targetColumnIndex, Integer rowIndex) {
//            this.exportFieldGroupCountList = getGroupCountList(exportDataList);
//            this.targetColumnIndex = targetColumnIndex;
//            this.rowIndex = rowIndex;
//        }
//
//
//        @Override
//        protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
//
//            // 仅从首行以及目标列的单元格开始合并，忽略其他
//            // if (cell.getRowIndex() == 2 && cell.getColumnIndex() == 0) {
//            //     // 合并单元格
//            //     CellRangeAddress cellRangeAddress = new CellRangeAddress(2, 3, 0, 0);
//            //     // sheet.addMergedRegionUnsafe(cellRangeAddress);
//            //     sheet.addMergedRegion(cellRangeAddress);
//            //     CellStyle cellStyle = cell.getCellStyle();
//            //     cellStyle.setAlignment(HorizontalAlignment.CENTER);
//            //     cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//            //     cellStyle.setWrapText(true);
//            // }
//
//            if (null == rowIndex) {
//                rowIndex = cell.getRowIndex() + 1;
//            }
//            // 仅从首行以及目标列的单元格开始合并，忽略其他
//            if (cell.getRowIndex() == rowIndex && cell.getColumnIndex() == targetColumnIndex) {
//                mergeGroupColumn(sheet);
//            }
//        }
//
//        private void mergeGroupColumn(Sheet sheet) {
//            int rowCount = rowIndex;
//            for (Integer count : exportFieldGroupCountList) {
//                if (count == 1) {
//                    rowCount += count;
//                    continue;
//                }
//                // 合并单元格
//                CellRangeAddress cellRangeAddress = new CellRangeAddress(rowCount, rowCount + count - 1, targetColumnIndex, targetColumnIndex);
//                sheet.addMergedRegionUnsafe(cellRangeAddress);
//                rowCount += count;
//            }
//        }
//
//        // 该方法将目标列根据值是否相同连续可合并，存储可合并的行数
//        private List<Integer> getGroupCountList(List<String> exportDataList) {
//            if (CollectionUtils.isEmpty(exportDataList)) {
//                return new ArrayList<>();
//            }
//            List<Integer> groupCountList = new ArrayList<>();
//            int count = 1;
//            for (int i = 1; i < exportDataList.size(); i++) {
//                if (exportDataList.get(i).equals(exportDataList.get(i - 1))) {
//                    count++;
//                } else {
//                    groupCountList.add(count);
//                    count = 1;
//                }
//            }
//            // 处理完最后一条后
//            groupCountList.add(count);
//            return groupCountList;
//        }
//
//        @Override
//        public void afterCellDispose(CellWriteHandlerContext context) {
//            merge(context.getWriteSheetHolder().getSheet(), context.getCell(), context.getHeadData(),
//                    context.getRelativeRowIndex());
//        }
//    }
//
//
//    public static class CustomColumnMergeStrategy extends AbstractMergeStrategy {
//        /**
//         * 分组，每几行合并一次
//         */
//        private final List<Integer> rowlist;
//        /**
//         * 开始列
//         */
//        private final int startColumn;
//        /**
//         * 结束列
//         */
//        private final int endColumn;
//
//        public CustomColumnMergeStrategy(List<Integer> rowlist, Integer startColumn, Integer endColumn) {
//            this.rowlist = rowlist;
//            this.startColumn = startColumn;
//            this.endColumn = endColumn;
//        }
//
//        @Override
//        protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
//            if (cell.getColumnIndex() == startColumn && rowlist.contains(cell.getRowIndex())) {
//                // 合并单元格
//                CellRangeAddress cellRangeAddress = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), startColumn, endColumn);
//                sheet.addMergedRegion(cellRangeAddress);
//                CellStyle cellStyle = cell.getCellStyle();
//                cellStyle.setAlignment(HorizontalAlignment.CENTER);
//                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//                cellStyle.setWrapText(true);
//                // cellStyle.setFont();
//            }
//        }
//
//        @Override
//        public void afterCellDispose(CellWriteHandlerContext context) {
//            merge(context.getWriteSheetHolder().getSheet(), context.getCell(), context.getHeadData(),
//                    context.getRelativeRowIndex());
//        }
//    }
//
//
//    /**
//     * 经费安排导出
//     */
//    public static Map<String, String> foundPlanExportExcel(Map<String, List<Map<String, Object>>> data, String fileName) {
//        String fullPath = buildFullPath("/ONLINE/XM/TMP/", fileName + new SimpleDateFormat("yyyyMMdd").format(new Date()));
//
//        class CustomizeColumnWidth extends AbstractColumnWidthStyleStrategy {
//            @Override
//            protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> list, Cell cell, Head head, Integer integer, Boolean isHead) {
//
//                Sheet sheet = writeSheetHolder.getSheet();
//                sheet.setColumnWidth(cell.getColumnIndex(), 5000);
//            }
//        }
//
//        // 头的策略
//        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
//        // 背景色
//        headWriteCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
//        WriteFont headWriteFont = new WriteFont();
//        headWriteFont.setFontHeightInPoints((short) 120);
//        headWriteCellStyle.setWriteFont(headWriteFont);
//        // 内容的策略
//        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
//        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
////        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
////        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
//        // 背景绿色
////        contentWriteCellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
//        // 字体策略
//        WriteFont contentWriteFont = new WriteFont();
//        // 字体大小
//        contentWriteFont.setFontHeightInPoints((short) 12);
//        contentWriteCellStyle.setWriteFont(contentWriteFont);
//
//        // 设置 自动换行
//        contentWriteCellStyle.setWrapped(true);
//        // 设置 垂直居中
//        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
////        //设置 水平居中
////        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
////        //设置边框样式
////        contentWriteCellStyle.setBorderLeft(DASHED);
////        contentWriteCellStyle.setBorderTop(DASHED);
////        contentWriteCellStyle.setBorderRight(DASHED);
////        contentWriteCellStyle.setBorderBottom(DASHED);
//
//        // 这个策略是 头是头的样式 内容是内容的样式
//        HorizontalCellStyleStrategy horizontalCellStyleStrategy =
//                new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
//
//
//        try (OutputStream outputStream = FileUtil.writeFile(fullPath);
//             ExcelWriter writer = EasyExcel.write(outputStream)
//                     .registerConverter(new LongStringConverter())
//                     .registerConverter(new LocalDateStringConverter())
////                     .registerWriteHandler(horizontalCellStyleStrategy)
//                     .registerWriteHandler(new CustomizeColumnWidth())
////                     .registerWriteHandler(new CustomCellWriteHandler())
//                     .build()
//        ) {
//
//            String[] sheetNames = {"总经费", "任务下达方费用化拨款安排", "任务下达方资本化拨款安排", "任务承担方自筹经费安排", "任务下达方外协项目安排", "任务承担方自筹经费外协项目安排"};
//            String unit = "(单位:万元)";
//
//            // 第一个个sheet的表头
//            List<List<String>> headList1 = new ArrayList<List<String>>() {{
//                add(Arrays.asList(sheetNames[0] + unit, "资金来源", "资金来源", "资金来源"));
//                add(Arrays.asList(sheetNames[0] + unit, "任务下达方拨款", "费用化", "总计"));
//                add(Arrays.asList(sheetNames[0] + unit, "任务下达方拨款", "费用化", "其中外协经费"));
//                add(Arrays.asList(sheetNames[0] + unit, "任务下达方拨款", "资本化", "资本化"));
//                add(Arrays.asList(sheetNames[0] + unit, "任务承担方自筹", "总计", "总计"));
//                add(Arrays.asList(sheetNames[0] + unit, "任务承担方自筹", "其中外协经费", "其中外协经费"));
//                add(Arrays.asList(sheetNames[0] + unit, "其他", "其他", "其他"));
//                add(Arrays.asList(sheetNames[0] + unit, "合计", "合计", "合计"));
//            }};
//
//            // 第一个sheet的数据
//            List<List<Object>> dataList1 = new ArrayList<>();
//            Map<String, Object> table1 = data.get("table-0").get(0);
//            if (table1 != null) {
//                dataList1.add(new ArrayList<Object>() {{
//                    add("金额");
//                    add(table1.get("费用化-总计"));
//                    add(table1.get("费用化-其中外协经费"));
//                    add(table1.get("资本化"));
//                    add(table1.get("任务承担方自筹-总计"));
//                    add(table1.get("任务承担方自筹-其中外协经费"));
//                    add(Strings.EMPTY);
//                    add(table1.get("合计"));
//                }});
//            }
//
//            WriteSheet sheet1 = EasyExcel.writerSheet("总经费").head(headList1).build();
//            writer.write(dataList1, sheet1);
//
//            //__________________________________________________________________________________________________________
//            for (int i = 1; i <= 3; i++) {
//
//                String company;
//                if (i == 3) {
//                    company = "筹款单位";
//                } else {
//                    company = "受款单位";
//                }
//
//                List<List<String>> headList234 = new ArrayList<>();
//                headList234.add(Arrays.asList(sheetNames[i] + unit, company));
//                headList234.add(Arrays.asList(sheetNames[i] + unit, "合计"));
//
//                List<List<Object>> dataList234 = new ArrayList<>();
//
//                List<Map<String, Object>> lines = data.get("table-" + i);
//                if (lines != null) {
//                    // 获取最大最小年
//                    List<Integer> yearList = getYearList(lines.stream().map(Map::keySet), "年");
//                    int finalI = i;
//                    yearList.forEach(e ->
//                            headList234.add(Arrays.asList(sheetNames[finalI] + unit, e + "年"))
//                    );
//
//                    lines.forEach(
//                            line -> dataList234.add(new ArrayList<Object>() {{
//                                add(line.get(company));
//                                add(line.get("合计"));
//                                // 按照年份list从小到大从map取值,放入导出的list
//                                yearList.forEach(year -> add(Optional.ofNullable(line.get(year + "年")).orElse(Strings.EMPTY)));
//                            }})
//                    );
//                }
//                WriteSheet sheet234 = EasyExcel.writerSheet(sheetNames[i]).head(headList234).build();
//                writer.write(dataList234, sheet234);
//            }
//
//            //__________________________________________________________________________________________________________
//            for (int i = 4; i <= 5; i++) {
//                int finalI = i;
//                List<List<String>> headList56 = new ArrayList<List<String>>() {{
//                    add(Arrays.asList(sheetNames[finalI] + unit, "外协委托单位"));
//                    add(Arrays.asList(sheetNames[finalI] + unit, "外协合同名称"));
//                    add(Arrays.asList(sheetNames[finalI] + unit, "外协经费"));
//                    add(Arrays.asList(sheetNames[finalI] + unit, "外协承担单位"));
//                    add(Arrays.asList(sheetNames[finalI] + unit, "合计"));
//                }};
//
//                List<List<Object>> dataList56 = new ArrayList<>();
//                List<Map<String, Object>> lines = data.get("table-" + finalI);
//                AtomicReference<Object> total = new AtomicReference<>();
//                if (lines != null) {
//                    lines.forEach(line -> {
//                        if (line.containsKey("合计")) {
//                            total.set(line.get("合计"));
//                        } else {
//                            dataList56.add(new ArrayList<Object>() {{
//                                add(line.get("外协委托单位"));
//                                add(line.get("外协合同名称"));
//                                add(line.get("外协经费"));
//                                add(line.get("外协承担单位"));
//                            }});
//                        }
//                    });
//                }
//
//                dataList56.add(new ArrayList<Object>() {{
//                    add(Strings.EMPTY);
//                    add(Strings.EMPTY);
//                    add(Strings.EMPTY);
//                    add(Strings.EMPTY);
//                    add(total.get());
//                }});
//
//                WriteSheet sheet56 = EasyExcel.writerSheet(sheetNames[i]).head(headList56).build();
//                writer.write(dataList56, sheet56);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        Map<String, String> result = Maps.newHashMapWithExpectedSize(2);
//        result.put(FileExportConstant.FILE_NAME, fileName + ".xlsx");
//        result.put(FileExportConstant.FILE_PATH, fullPath);
//        return result;
//    }
//
//
//    /**
//     * 获取年份(yyyy)集合,有序,最小到最大
//     *
//     * @param suffix 年份后缀
//     */
//    public static List<Integer> getYearList(Stream<Collection<String>> elements, String suffix) {
//        return elements.flatMap(Collection::stream)
//                .filter(e -> e.matches("\\d{4}" + suffix))
//                .map(key -> Integer.parseInt(key.replaceAll("[^\\d{4}]", Strings.EMPTY)))
//                .distinct()
//                .sorted()
//                .collect(Collectors.toList());
//    }
//
//
//}
