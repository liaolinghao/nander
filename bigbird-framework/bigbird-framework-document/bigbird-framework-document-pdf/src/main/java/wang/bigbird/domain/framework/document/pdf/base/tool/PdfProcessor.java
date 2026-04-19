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
package wang.bigbird.domain.framework.document.pdf.base.tool;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.CommandLineApp;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;


/**
 * PDF文档处理器，提供围绕PDF处理的公共方法
 * 如果要使用该工具中的导出表格方法，需要把字体文件安装到系统
 *
 * @author Bigbird
 */
@Slf4j
public class PdfProcessor {

    /**
     * PDF绘制的默认字体
     */
    private static Font defaultFont;

    static {
        InputStream stream = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("/font/simhei.ttf");
            stream = classPathResource.getStream();
            byte[] st1 = new byte[stream.available()];
            stream.read(st1);
            BaseFont bf = BaseFont.createFont("simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, BaseFont.NOT_CACHED,
                    st1, st1);
            defaultFont = new Font(bf, 15, Font.NORMAL, BaseColor.BLACK);
        } catch (DocumentException | IOException e) {
            log.error("CreateDefaultFont:", e);
        } finally {
            StreamUtils.close(stream);
        }
    }


    /**
     * 禁止构造转化器实例
     */
    private PdfProcessor() {
    }

    /**
     * 验证文件有效性
     *
     * @param file PDF文件
     */
    public static void verifyFile(File file) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.isTrue(file.exists() && FileUtils.isPdfFile(file), "The parameter file is not a valid pdf file.");
    }

    /**
     * 判断PDF文件是否包含JS脚本
     *
     * @param file PDF文件
     * @return 是否包含JS脚本
     */
    public static boolean isContainJavaScript(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        List<COSObject> pdfObjects = document.getDocument().getObjects();
        for (COSObject object : pdfObjects) {
            COSBase realObject = object.getObject();
            if (realObject instanceof COSDictionary) {
                String str = realObject.toString();
                if (str.contains("COSName{JS}") || str.contains("COSName{JavaScript}")) {
                    return true;
                }
            } else if (realObject instanceof COSName
                    && isJSObject((COSName) realObject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是JS对象
     *
     * @param realObject
     * @return
     */
    private static boolean isJSObject(COSName realObject) {
        return COSName.JS.equals(realObject) || COSName.JAVA_SCRIPT.equals(realObject);
    }

    /**
     * 将pdf表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     * 注意，该方法不会读取PDF文档中表格里面的图片数据，也不会读取表格之外的数据。
     * 此外，如果表格以图片形式插入到PDF中，该表格数据也不会读取。
     *
     * @param file pdf文档
     * @return pdf文档中所有表格包含的数据集合，按照表格序号值（从1开始计算）为key，表格数据集合为value进行组织
     */
    public static Map<Integer, ArrayList<String[]>> getTableData(File file) throws ParseException {
        verifyFile(file);
        Map<Integer, ArrayList<String[]>> map = new LinkedHashMap<>();
        String[] args = new String[]{"-f=JSON", "-p=all", file.getAbsolutePath(), "-l"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);
        StringBuffer stringBuffer = new StringBuffer();
        new CommandLineApp(stringBuffer, cmd).extractTables(cmd);
        String pdfTableString = StringUtils.trimAllWhitespace(stringBuffer.toString());
        JSONArray jsonArray = new JSONArray(pdfTableString);
        int tableSize = jsonArray.size();
        int index = 1;
        for (int i = 0; i < tableSize; i++) {
            JSONObject tableData = jsonArray.getJSONObject(i);
            JSONArray data = tableData.getJSONArray("data");
            if (data.size() > 0) {
                // 提取表格数据
                map.put(index, extractTableData(data));
                index++;
            }
        }
        return map;
    }

    /**
     * 解析json数组，把表格数据转换为二维集合返回
     *
     * @param jsonArray 包含表格数据的json数组
     * @return 以二维集合形式包装的表格数据
     */
    private static ArrayList<String[]> extractTableData(JSONArray jsonArray) {
        ArrayList<String[]> tableData = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray row = jsonArray.getJSONArray(i);
            List<String> rowData = new ArrayList<>();
            for (int j = 0; j < row.size(); j++) {
                JSONObject col = row.getJSONObject(j);
                rowData.add(col.getStr("text"));
            }
            tableData.add(rowData.toArray(new String[]{}));
        }
        return tableData;
    }

    /**
     * 将pdf文本内容逐页读取，该方法适合小页数范围的PDF内容获取，
     * 大页数范围的PDF内容获取使用该方法有可能导致内存占用过大，此时建议先获取PDF页码，再获取指定页内容
     *
     * @param file pdf文档
     * @return 按照页码顺序组织的文本内容集合
     */
    public static ArrayList<String> getTextData(File file) throws IOException {
        verifyFile(file);
        ArrayList<String> pages = new ArrayList<>();
        PdfReader reader = null;
        try {
            reader = new PdfReader(file.getAbsolutePath());
            int numberOfPages = reader.getNumberOfPages();
            for (int pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
                String textFromPage = PdfTextExtractor.getTextFromPage(reader, pageNumber);
                pages.add(textFromPage);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return pages;
    }

    /**
     * 将pdf文本内容逐页读取，该方法适合小页数范围的PDF内容获取，
     * 大页数范围的PDF内容获取使用该方法有可能导致内存占用过大，此时建议先获取PDF页码，再获取指定页内容
     *
     * @param file pdf文档
     * @return PDF文档的总页数
     */
    public static int getPageNum(File file) throws IOException {
        verifyFile(file);
        PdfReader reader = null;
        try {
            reader = new PdfReader(file.getAbsolutePath());
            return reader.getNumberOfPages();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 获取PDF文档指定页的文本内容
     *
     * @param file    pdf文档
     * @param pageNum 指定页码
     * @return 指定页内容
     */
    public static String getPageContent(File file, int pageNum) throws IOException {
        verifyFile(file);
        Assert.isTrue(pageNum > 0,
                "The parameter pageNum must be greater than zero.");
        PdfReader reader = null;
        try {
            reader = new PdfReader(file.getAbsolutePath());
            int numberOfPages = reader.getNumberOfPages();
            Assert.isTrue(pageNum > numberOfPages,
                    StringUtils.joinStr("The parameter pageNum exceeds the total number of pages:", numberOfPages));
            return PdfTextExtractor.getTextFromPage(reader, pageNum);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers 表格属性列名数组
     * @param list    需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list, OutputStream out) throws DocumentException {
        exportPdf(headers, list, null, out);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers          表格属性列名数组
     * @param list             需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                         javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap 行的背景颜色风格，BaseColor内置各种颜色值
     * @param out              与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, OutputStream out) throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, null, out);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap, OutputStream out)
            throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, rowHeightStyleMap, null, out);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个sheet
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap,
                                 Map<Integer, Float> colWidthStyleMap, OutputStream out) throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, null, out);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap,
                                 Map<Integer, Float> colWidthStyleMap, Set<String> imageColSet, OutputStream out) throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, out, null);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap,
                                 Map<Integer, Float> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String pattern) throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, out, pattern, null);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @param document          PDF文档尺寸，设置为null，默认采用A4大小
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap,
                                 Map<Integer, Float> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String pattern, Document document) throws DocumentException {
        exportPdf(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, out, pattern, document, null);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以PDF的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将PDF文档导出到本地文件或者网络中
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @param document          PDF文档尺寸，设置为null，默认采用A4大小
     * @param font              采用的字体
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    public static void exportPdf(Map<String, String> headers, Collection<Map<String, Object>> list,
                                 Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap,
                                 Map<Integer, Float> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String pattern, Document document, Font font) throws DocumentException {
        Assert.notNull(headers, "The parameter headers is null.");
        Assert.notNull(list, "The parameter list is null.");
        Assert.notNull(out, "The parameter out is null.");
        if (document == null) {
            document = new Document();
        }
        if (font == null) {
            // 采用自带的默认字体
            font = defaultFont;
        }
        document.open();
        try {
            // 建立一个书写器(Writer)与document对象关联，通过书写器(Writer)可以将文档写入到输出流。
            PdfWriter.getInstance(document, out);
            document.open();
            drawTable(document, font, headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet,
                    pattern);
        } finally {
            document.close();
        }
    }

    /**
     * 绘制数据表格
     *
     * @param document          待绘制的PDF文档
     * @param font              采用的字体
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @throws DocumentException 文档处理过程中，可能发生异常
     */
    private static void drawTable(Document document, Font font, Map<String, String> headers, Collection<Map<String, Object>> list, Map<Integer, BaseColor> rowColorStyleMap, Map<Integer, Float> rowHeightStyleMap, Map<Integer, Float> colWidthStyleMap, Set<String> imageColSet, String pattern) throws DocumentException {
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        //根据header的个数设置pdf列数
        int colSize = headers.size();
        PdfPTable dataTable = new PdfPTable(colSize);
        //分别设置每列的宽度
        float[] cellsWidth = setColWidth(colWidthStyleMap, colSize);
        dataTable.setWidths(cellsWidth);
        drawTableHeader(headers, rowHeightStyleMap, rowColorStyleMap, dataTable, font);
        // 表格头标识，通过该标识与具体的列数据对应起来
        Set<String> keys = headers.keySet();
        Iterator<Map<String, Object>> it = list.iterator();
        int row = 0;
        while (it.hasNext()) {
            row++;
            Map<String, Object> map = it.next();
            // 列索引
            int cellNum = 0;
            // 遍历列名
            for (String key : keys) {
                Object value = map.get(key);
                PdfPCell cell;
                if (imageColSet != null && imageColSet.contains(key) && value != null) {
                    if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                        try {
                            cell = setCellImageAndAttr((String) value, cellsWidth[cellNum], row, rowHeightStyleMap, rowColorStyleMap);
                        } catch (Exception e) {
                            log.error("SetCellImageAndAttr:", e);
                            cell = setCellContentAndAttr(sdf, value, row, rowHeightStyleMap, rowColorStyleMap, font);
                        }
                    } else {
                        cell = setCellContentAndAttr(sdf, CommonConstants.EMPTY, row, rowHeightStyleMap, rowColorStyleMap, font);
                    }
                } else {
                    cell = setCellContentAndAttr(sdf, value, row, rowHeightStyleMap, rowColorStyleMap, font);
                }
                dataTable.addCell(cell);
                cellNum++;
            }
        }
        document.add(dataTable);
    }

    /**
     * 该方法用于设置图片的单元格
     *
     * @param value             图片路径
     * @param cellWidth         单元格宽度
     * @param row               所在行索引，从0开始计算
     * @param rowHeightStyleMap 行的高度风格
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @return 图片内容的表格单元格
     * @throws IOException         处理过程中可能抛出该异常
     * @throws BadElementException 处理过程中可能抛出该异常
     */
    private static PdfPCell setCellImageAndAttr(String value, float cellWidth, int row, Map<Integer, Float> rowHeightStyleMap, Map<Integer, BaseColor> rowColorStyleMap) throws IOException, BadElementException {
        Image img = Image.getInstance(value);
        img.setAlignment(Image.ALIGN_CENTER);
        PdfPCell cell = new PdfPCell(img, true);
        setRowCellStyle(cell, row, rowHeightStyleMap, rowColorStyleMap);
        return cell;
    }

    /**
     * 该方法用于设置非图片的单元格
     *
     * @param sdf               日期格式
     * @param value             单元格值
     * @param row               所在行索引，从0开始计算
     * @param rowHeightStyleMap 行的高度风格
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param font              采用的字体
     * @return 文本内容的表格单元格
     */
    private static PdfPCell setCellContentAndAttr(SimpleDateFormat sdf, Object value, int row, Map<Integer, Float> rowHeightStyleMap, Map<Integer, BaseColor> rowColorStyleMap, Font font) {
        PdfPCell cell;
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            cell = getPdfPCell(font, intValue);
        } else if (value instanceof Float) {
            float fValue = (Float) value;
            cell = getPdfPCell(font, fValue);
        } else if (value instanceof Double) {
            double dValue = (Double) value;
            cell = getPdfPCell(font, dValue);
        } else if (value instanceof Long) {
            long longValue = (Long) value;
            cell = getPdfPCell(font, longValue);
        } else if (value instanceof Boolean) {
            boolean bValue = (Boolean) value;
            cell = getPdfPCell(font, bValue);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            String textValue = sdf.format(date);
            cell = getPdfPCell(font, textValue);
        } else if (value instanceof String[]) {
            String[] strArr = (String[]) value;
            StringBuilder sb = new StringBuilder();
            for (String str : strArr) {
                if (StringUtils.isNotBlank(str)) {
                    sb.append(str).append(CommonConstants.COMMA);
                }
            }
            cell = getPdfPCell(font, sb.substring(0, sb.length() - 1));
        } else if (value instanceof Double[]) {
            Double[] douArr = (Double[]) value;
            StringBuilder sb = new StringBuilder();
            for (Double val : douArr) {
                if (val != null) {
                    sb.append(val).append(CommonConstants.COMMA);
                }
            }
            cell = getPdfPCell(font, sb.substring(0, sb.length() - 1));
        } else {
            // 其它数据类型都当作字符串简单处理
            String textValue = value == null ? CommonConstants.EMPTY : value.toString();
            cell = getPdfPCell(font, textValue);
        }
        setRowCellStyle(cell, row, rowHeightStyleMap, rowColorStyleMap);
        return cell;
    }

    /**
     * 设置列宽
     *
     * @param colWidthStyleMap 列的宽度风格
     * @param colSize          列数
     * @return 列宽数组
     */
    private static float[] setColWidth(Map<Integer, Float> colWidthStyleMap, int colSize) {
        float[] cellsWidth = new float[colSize];
        //设置默认值
        for (int i = 0; i < colSize; i++) {
            cellsWidth[i] = 1;
        }
        if (colWidthStyleMap != null) {
            for (int i = 0; i < colSize; i++) {
                cellsWidth[i] = colWidthStyleMap.get(i);
            }
        }
        return cellsWidth;
    }

    /**
     * 绘制表格头
     *
     * @param headers           表格属性列名数组
     * @param rowHeightStyleMap 行的高度风格
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     * @param dataTable         待绘制的表格
     * @param font              采用的字体
     */
    private static void drawTableHeader(Map<String, String> headers, Map<Integer, Float> rowHeightStyleMap, Map<Integer, BaseColor> rowColorStyleMap, PdfPTable dataTable, Font font) {
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String headerContent = entry.getValue();
            //设置内容
            PdfPCell cell = new PdfPCell(getPdfPCell(font, headerContent));
            setRowCellStyle(cell, 0, rowHeightStyleMap, rowColorStyleMap);
            dataTable.addCell(cell);
        }
    }

    /**
     * 设置对应行单元格的高度与内容居中
     *
     * @param cell              表格单元格
     * @param row               所在行索引，从0开始计算
     * @param rowHeightStyleMap 行的高度风格
     * @param rowColorStyleMap  行的背景颜色风格，BaseColor内置各种颜色值
     */
    private static void setRowCellStyle(PdfPCell cell, int row, Map<Integer, Float> rowHeightStyleMap, Map<Integer, BaseColor> rowColorStyleMap) {
        //设置居中
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //设置行高
        if (rowHeightStyleMap != null && rowHeightStyleMap.get(row) != null) {
            cell.setFixedHeight(rowHeightStyleMap.get(row));
        }
        //设置背景颜色
        if (rowColorStyleMap != null && rowColorStyleMap.get(row) != null) {
            cell.setBackgroundColor(rowColorStyleMap.get(row));
        }
    }

    /**
     * 构造PDF表格单元格
     *
     * @param font      绘制单元格的字体，对于中文必须设置有效的中文字体才能正常显示
     * @param cellValue 单元格文本值
     * @return 返回PDF表格单元格
     */
    private static PdfPCell getPdfPCell(Font font, Object cellValue) {
        Phrase phrase = new Phrase();
        if (font != null) {
            phrase.setFont(font);
        }
        phrase.add(String.valueOf(cellValue));
        return new PdfPCell(phrase);
    }

}
