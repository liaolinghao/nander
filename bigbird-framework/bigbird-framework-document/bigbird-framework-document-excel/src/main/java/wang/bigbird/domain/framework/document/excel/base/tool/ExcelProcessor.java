/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.document.excel.base.tool;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * Excel文档处理器，提供围绕Excel处理的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public class ExcelProcessor {

    /**
     * 2003格式的excel文件
     */
    public static final String EXTENSION_XLS = "xls";
    /**
     * 2007格式的excel文件
     */
    public static final String EXTENSION_XLSX = "xlsx";

    /**
     * 防止类实例化
     */
    private ExcelProcessor() {
    }

    /**
     * 验证文件有效性
     *
     * @param file excel文件
     */
    public static void verifyFile(File file) {
        Assert.notNull(file,
                "The parameter file is null.");
        Assert.isTrue(file.exists() && FileUtils.isExcelFile(file), "The parameter file is not a valid excel file.");
    }

    /***
     *
     * 取得Workbook对象(xls和xlsx对象不同,不过都是Workbook的实现类) xls:HSSFWorkbook
     * xlsx：XSSFWorkbook，该方法不适合处理大数据量的文件
     *
     * @param file excel文档
     * @return excel文档对应的Workbook对象
     * @throws IOException 文档读取过程中，可能发生异常
     *
     */
    public static Workbook getWorkbook(File file) throws IOException {
        verifyFile(file);
        // Java 7引入的try-with-resources语法可以自动管理资源，确保在退出块时关闭所有声明的资源。
        // 这样可以保证无论是否发生异常，输入流都会被正确关闭。
        try (InputStream is = new FileInputStream(file)) {
            if (file.getName().endsWith(EXTENSION_XLS)) {
                return new HSSFWorkbook(is);
            } else if (file.getName().endsWith(EXTENSION_XLSX)) {
                return new XSSFWorkbook(is);
            }
            throw new IOException("Unsupported file format");
        }
    }

    /**
     * 取单元格的值
     *
     * @param cell       单元格对象
     * @param treatAsStr 为true时，当做文本来取值 (取到的是文本，不会把“1”取成“1.0”)
     * @return 单元格包含的数据值
     */
    public static String getCellValue(Cell cell, boolean treatAsStr) {
        if (cell == null) {
            return "";
        }
        if (treatAsStr) {
            // 虽然excel中设置的都是文本，但是数字文本还被读错，如“1”取成“1.0”
            // 单元格按照文本来处理
            cell.setCellType(CellType.STRING);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return String.valueOf(cell.getStringCellValue());
        }
    }

    /**
     * 将excel表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @param file excel文档
     * @return excel文档第一个Sheet包含的数据集合
     * @throws IOException 获取数据过程中可能会抛出该异常
     */
    public static ArrayList<String[]> getTableData(File file) throws IOException {
        return getTableData(file, 0);
    }

    /**
     * 将excel表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @param file       excel文档
     * @param sheetIndex sheet索引
     * @return excel文档指定Sheet包含的数据集合
     * @throws IOException 获取数据过程中可能会抛出该异常
     */
    public static ArrayList<String[]> getTableData(File file, int sheetIndex) throws IOException {
        verifyFile(file);
        Assert.isTrue(sheetIndex >= 0, "The parameter sheetIndex is negative.");
        ArrayList<String[]> tableData = new ArrayList<>();
        Workbook workbook = null;
        try {
            workbook = ExcelProcessor.getWorkbook(file);
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            int rowNum = sheet.getLastRowNum();
            for (int i = 0; i <= rowNum; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int j = 0;
                    List<String> rowData = new ArrayList<>();
                    int colNum = row.getLastCellNum();
                    while (j < colNum) {
                        String value = ExcelProcessor.getCellValue(row.getCell(j), true);
                        rowData.add(value.trim());
                        j++;
                    }
                    tableData.add(rowData.toArray(new String[]{}));
                }
            }
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error("Close:", e);
                }
            }
        }
        return tableData;
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers 表格属性列名数组
     * @param list    需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type    excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list, OutputStream out,
                                   String type) throws IOException {
        exportExcel(headers, list, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers          表格属性列名数组
     * @param list             需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                         javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap 行的背景颜色风格
     * @param out              与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type             excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list,
                                   Map<Integer, Short> rowColorStyleMap, OutputStream out, String type) throws IOException {
        exportExcel(headers, list, rowColorStyleMap, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type              excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list,
                                   Map<Integer, Short> rowColorStyleMap, Map<Integer, Short> rowHeightStyleMap, OutputStream out, String type)
            throws IOException {
        exportExcel(headers, list, rowColorStyleMap, rowHeightStyleMap, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type              excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list,
                                   Map<Integer, Short> rowColorStyleMap, Map<Integer, Short> rowHeightStyleMap,
                                   Map<Integer, Integer> colWidthStyleMap, OutputStream out, String type) throws IOException {
        exportExcel(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, null, out, null, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type              excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list,
                                   Map<Integer, Short> rowColorStyleMap, Map<Integer, Short> rowHeightStyleMap,
                                   Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String type)
            throws IOException {
        exportExcel(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, out, null, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以EXCEL的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @param type              excel文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportExcel(Map<String, String> headers, Collection<Map<String, Object>> list,
                                   Map<Integer, Short> rowColorStyleMap, Map<Integer, Short> rowHeightStyleMap,
                                   Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String pattern,
                                   String type) throws IOException {
        Assert.notNull(headers, "The parameter headers is null.");
        Assert.notNull(list, "The parameter list is null.");
        Assert.notNull(out, "The parameter out is null.");
        // 声明一个工作薄
        Workbook workbook;
        if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(EXTENSION_XLS)) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new SXSSFWorkbook();
        }
        try {
            // 生成一个表格
            write2Sheet(workbook, headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet,
                    pattern, type);
            workbook.write(out);
        } finally {
            StreamUtils.close(workbook, out);
        }
    }

    /**
     * 写入数据到指定的Sheet
     *
     * @param workbook          工作表
     * @param headers           表头
     * @param list              数据集合
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param pattern           日期格式
     * @param type              excel文件类型
     */
    private static void write2Sheet(Workbook workbook, Map<String, String> headers,
                                    Collection<Map<String, Object>> list, Map<Integer, Short> rowColorStyleMap,
                                    Map<Integer, Short> rowHeightStyleMap, Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet,
                                    String pattern, String type) {
        // 时间格式默认"yyyy-MM-dd"
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        Sheet sheet = workbook.createSheet();
        // POI单元格样式创建过多，会出现错误，此处将单元格样式提取出来，防止出错
        CellStyle style = sheet.getWorkbook().createCellStyle();
        FillPatternType fillPattern = style.getFillPattern();
        short fillForegroundColor = style.getFillForegroundColor();
        // 产生表格标题行
        Row row = sheet.createRow(0);
        // 构造行的背景颜色
        if (rowColorStyleMap != null && rowColorStyleMap.get(0) != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(rowColorStyleMap.get(0));
        } else {
            style.setFillPattern(fillPattern);
            style.setFillForegroundColor(fillForegroundColor);
        }
        // 构造行的高度
        if (rowHeightStyleMap != null && rowHeightStyleMap.get(0) != null) {
            row.setHeightInPoints(rowHeightStyleMap.get(0));
        }
        Set<String> keys = headers.keySet();
        Iterator<String> it1 = keys.iterator();
        // 标题列数
        int c = 0;
        while (it1.hasNext()) {
            // 宽度单位是一个字符宽度的1/256，因此这里先*256获取一个字符宽度，然后*指定字符数量获得宽度
            if (colWidthStyleMap != null && colWidthStyleMap.get(c) != null) {
                sheet.setColumnWidth(c, colWidthStyleMap.get(c) * 256);
            }
            Cell cell = row.createCell(c);
            cell.setCellStyle(style);
            if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(EXTENSION_XLS)) {
                HSSFRichTextString text = new HSSFRichTextString(headers.get(it1.next()));
                cell.setCellValue(text);
            } else {
                XSSFRichTextString text = new XSSFRichTextString(headers.get(it1.next()));
                cell.setCellValue(text);
            }
            c++;
        }
        // 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        Drawing drawing = sheet.createDrawingPatriarch();
        // 遍历集合数据，产生数据行
        Iterator<Map<String, Object>> it = list.iterator();
        // 行索引
        int rowIndex = 0;
        while (it.hasNext()) {
            rowIndex++;
            row = sheet.createRow(rowIndex);
            // 构造行的背景颜色
            if (rowColorStyleMap != null && rowColorStyleMap.get(rowIndex) != null) {
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(rowColorStyleMap.get(rowIndex));
            } else {
                style.setFillPattern(fillPattern);
                style.setFillForegroundColor(fillForegroundColor);
            }
            // 构造行的高度
            if (rowHeightStyleMap != null && rowHeightStyleMap.get(rowIndex) != null) {
                row.setHeightInPoints(rowHeightStyleMap.get(rowIndex));
            }
            // 逐个单元格设置数据
            Map<String, Object> map = it.next();
            // 列索引
            int colIndex = 0;
            // 遍历列名
            for (String key : keys) {
                Object value = map.get(key);
                if (imageColSet != null && imageColSet.contains(key) && value != null) {
                    if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                        try {
                            setCellImageAndAttr(workbook, drawing, colIndex, rowIndex, value, type);
                        } catch (Exception ex) {
                            log.error("SetCellImageAndAttr:", ex);
                            setCellContentAndAttr(row, colIndex, style, value, formatter);
                        }
                    } else {
                        setCellContentAndAttr(row, colIndex, style, value, formatter);
                    }
                } else {
                    setCellContentAndAttr(row, colIndex, style, value, formatter);
                }
                colIndex++;
            }
        }
    }

    /**
     * 设置单元格图像内容
     *
     * @param workbook 工作表对象
     * @param drawing  绘制器
     * @param colIndex 列索引
     * @param rowIndex 行索引
     * @param value    图像路径
     * @param type     excel文件类型
     * @throws IOException 处理过程中可能抛出该异常
     */
    private static void setCellImageAndAttr(Workbook workbook, Drawing drawing, int colIndex, int rowIndex, Object value, String type) throws IOException {
        // 插入图片
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        try {
            BufferedImage img = ImageIO.read(new File((String) value));
            ImageIO.write(img, "jpg", byteArrayOut);
            ClientAnchor anchor;
            if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(EXTENSION_XLS)) {
                anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) colIndex, rowIndex, (short) colIndex,
                        rowIndex);
            } else {
                anchor = new XSSFClientAnchor(0, 0, 255, 255, colIndex, rowIndex, colIndex, rowIndex);
            }
            anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
            Picture picture = drawing.createPicture(anchor,
                    workbook.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_JPEG));
            picture.resize(1, 1);
        } finally {
            StreamUtils.close(byteArrayOut);
        }
    }

    /**
     * 设置单元格文本内容
     *
     * @param row       行对象
     * @param colIndex  列索引
     * @param style     单元格样式
     * @param value     单元格值
     * @param formatter 日期格式器
     */
    private static void setCellContentAndAttr(Row row, int colIndex, CellStyle style, Object value, DateTimeFormatter formatter) {
        Cell cell = row.createCell(colIndex);
        cell.setCellStyle(style);
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            cell.setCellValue(intValue);
        } else if (value instanceof Float) {
            float fValue = (Float) value;
            cell.setCellValue(fValue);
        } else if (value instanceof Double) {
            double dValue = (Double) value;
            cell.setCellValue(dValue);
        } else if (value instanceof Long) {
            long longValue = (Long) value;
            cell.setCellValue(longValue);
        } else if (value instanceof Boolean) {
            boolean bValue = (Boolean) value;
            cell.setCellValue(bValue);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            String textValue = formatter.format(date.toInstant());
            cell.setCellValue(textValue);
        } else if (value instanceof String[]) {
            String[] strArr = (String[]) value;
            StringBuilder sb = new StringBuilder();
            for (String str : strArr) {
                if (StringUtils.isNotBlank(str)) {
                    sb.append(str).append(",");
                }
            }
            cell.setCellValue(sb.substring(0, sb.length() - 1));
        } else if (value instanceof Double[]) {
            Double[] douArr = (Double[]) value;
            StringBuilder sb = new StringBuilder();
            for (Double val : douArr) {
                if (val != null) {
                    sb.append(val).append(",");
                }
            }
            cell.setCellValue(sb.substring(0, sb.length() - 1));
        } else {
            // 其它数据类型都当作字符串简单处理
            String textValue = value == null ? "" : value.toString();
            cell.setCellValue(textValue);
        }
    }

}
