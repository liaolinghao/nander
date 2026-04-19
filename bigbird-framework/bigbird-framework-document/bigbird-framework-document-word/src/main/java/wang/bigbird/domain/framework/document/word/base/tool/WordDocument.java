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
package wang.bigbird.domain.framework.document.word.base.tool;

import com.deepoove.poi.XWPFTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.document.api.base.util.KeyUtils;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Word文档处理器，包装了2003格式和2007格式的word文档处理逻辑
 *
 * @author Bigbird
 */
@Slf4j
public class WordDocument implements Closeable {

    /**
     * 默认表格行高，计算方式：10.5*20，尺寸计算原理为：
     * 默认采用字体为：等线 (中文正文)五号 五号对应10.5 Point
     * 一个Point对应20个单位
     */
    private static final int DEFAULT_ROW_HEIGHT = 210;
    /**
     * 表格最大宽，计算方式：10.5*39*20 = 8190 ，尺寸计算原理为：
     * 默认采用字体为：等线 (中文正文)五号 五号对应10.5 Point
     * 一个Point对应20个单位
     * A4纸，五号字，默认页边距的话：每页44行，每行39个字
     */
    private static final int DEFAULT_MAX_WIDTH = 8190;

    /**
     * 2003格式word的文档处理器
     */
    private HWPFDocument doc2003;
    /**
     * 2007格式word的文档处理器
     */
    private XWPFDocument doc2007;
    /**
     * 2007格式word的文档处理器增强版
     */
    private XWPFTemplate doc2007plus;

    /**
     * 包装了2003格式的文档处理器
     *
     * @param doc2003 2003格式的文档处理器
     */
    public WordDocument(HWPFDocument doc2003) {
        this.doc2003 = doc2003;
    }

    /**
     * 包装了2007格式的文档处理器
     *
     * @param doc2007 2007格式的文档处理器
     */
    public WordDocument(XWPFDocument doc2007) {
        this.doc2007 = doc2007;
    }

    /**
     * 包装了2007格式的文档处理器
     *
     * @param doc2007plus 2007格式的文档处理器增强版
     */
    public WordDocument(XWPFTemplate doc2007plus) {
        this.doc2007plus = doc2007plus;
    }

    /**
     * 替换文档中的指定文本
     *
     * @param texts 替换文本的映射关系
     */
    public void replaceText(LinkedHashMap<String, String> texts) {
        if (CollectionUtils.isNullOrEmpty(texts)) {
            return;
        }
        if (doc2003 != null) {
            replaceText4Doc2003(texts);
        } else if (doc2007 != null) {
            replaceText4Doc2007(texts);
        } else if (doc2007plus != null) {
            replaceText4Doc2007plus(texts);
        }
    }

    /**
     * 文档写入到指定的输出流
     *
     * @param outputStream 文档数据输出流
     * @throws IOException 输出文档数据过程中，可能抛出该异常
     */
    public void write(OutputStream outputStream) throws IOException {
        if (doc2003 != null) {
            doc2003.write(outputStream);
        } else if (doc2007 != null) {
            doc2007.write(outputStream);
        } else if (doc2007plus != null) {
            doc2007plus.write(outputStream);
        }
    }

    /**
     * 替换2003格式文档中的指定文本，可以保障替换文本的样式不变，但是该方法替换文本存在缺陷，对于部分word文档，
     * 可能造成表格错行
     *
     * @param texts 替换文本的映射关系
     */
    private void replaceText4Doc2003(LinkedHashMap<String, String> texts) {
        Range bodyRange = doc2003.getRange();
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            bodyRange.replaceText(entry.getKey(),
                    entry.getValue());
        }
    }

    /**
     * 替换2007格式文档中的指定文本，可以保障替换文本的样式不变，但是该方法替换文本存在缺陷，原因在于：
     * <p>
     * 该方法为了保留原文本的样式，会根据样式分组，对于部分word文档，一个段落内的文本分组可能出错，
     * 样式相同的文本会被划分到两个甚至多个组中，导致替换失败
     *
     * @param texts 替换文本的映射关系
     */
    private void replaceText4Doc2007(LinkedHashMap<String, String> texts) {
        replaceParagraphText(doc2007.getParagraphs(), texts);
        replaceTableText(doc2007.getTables(), texts);
    }

    /**
     * 替换表格中的文本内容
     *
     * @param tables 待替换文本的表格
     * @param texts  替换文本的映射关系
     */
    private void replaceTableText(List<XWPFTable> tables, LinkedHashMap<String, String> texts) {
        for (XWPFTable table : tables) {
            List<XWPFTableRow> rows = table.getRows();
            for (XWPFTableRow row : rows) {
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    replaceParagraphText(cell
                            .getParagraphs(), texts);
                }
            }
        }
    }

    /**
     * 替换段落中的文本内容
     *
     * @param paragraphs 待替换文本的段落
     * @param texts      替换文本的映射关系
     */
    private void replaceParagraphText(List<XWPFParagraph> paragraphs, LinkedHashMap<String, String> texts) {
        if (CollectionUtils.isNullOrEmpty(paragraphs)) {
            return;
        }
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if (StringUtils.isNotBlank(text)) {
                    boolean isSetText = false;
                    for (Map.Entry<String, String> entry : texts.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (text.contains(key)) {
                            isSetText = true;
                            text = text.replace(key, value);
                        }
                        if (isSetText) {
                            run.setText(text, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换2007格式文档中的指定文本，可以保障替换文本的样式不变
     *
     * @param texts 替换文本的映射关系
     */
    private void replaceText4Doc2007plus(LinkedHashMap<String, String> texts) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>(texts);
        render(data);
    }

    /**
     * 替换文档中的指定标签，该方法处理后，会将文档中的所有标签都去掉，所以在制作文档前应该只调用一次最佳
     *
     * @param data 替换数据的映射关系
     */
    public void render(LinkedHashMap<String, Object> data) {
        if (CollectionUtils.isNullOrEmpty(data)) {
            return;
        }
        LinkedHashMap<String, Object> filterData = new LinkedHashMap<>();
        Set<Map.Entry<String, Object>> entrys = data.entrySet();
        for (Map.Entry<String, Object> entry : entrys) {
            filterData.put(KeyUtils.filterTag(entry.getKey()), entry.getValue());
        }
        doc2007plus.render(filterData);
    }

    @Override
    public void close() {
        StreamUtils.close(doc2003, doc2007, doc2007plus);
    }

    /**
     * 将word表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     * 注意，该方法不会读取WORD文档中表格里面的图片数据，也不会读取表格之外的数据。
     *
     * @return word文档中所有表格包含的数据集合，按照表格序号值（从1开始计算）为key，表格数据集合为value进行组织
     */
    public Map<Integer, ArrayList<String[]>> getTableData() {
        if (doc2003 != null) {
            return getTableDataForDoc();
        } else if (doc2007 != null) {
            return getTableDataForDocx();
        }
        return null;
    }

    /**
     * 将word表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @return word文档中所有表格包含的数据集合，按照表格序号值（从1开始计算）为key，表格数据集合为value进行组织
     */
    private Map<Integer, ArrayList<String[]>> getTableDataForDocx() {
        Map<Integer, ArrayList<String[]>> tableDataMap = new LinkedHashMap<>();
        List<IBodyElement> elements = doc2007.getBodyElements();
        int index = 1;
        for (IBodyElement element : elements) {
            if (element instanceof XWPFTable) {
                ArrayList<String[]> tableData = getTableData((XWPFTable) element);
                tableDataMap.put(index, tableData);
                index++;
            }
        }
        return tableDataMap;
    }

    /**
     * 将excel表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @param table 表格元素
     * @return 表格包含的数据集合
     */
    private ArrayList<String[]> getTableData(XWPFTable table) {
        ArrayList<String[]> list = new ArrayList<>();
        List<XWPFTableRow> rows = table.getRows();
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            String[] data = new String[cells.size()];
            int c = 0;
            for (XWPFTableCell cell : cells) {
                String text = cell.getText();
                data[c++] = text;
            }
            list.add(data);
        }
        return list;
    }

    /**
     * 将word表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @return word文档中所有表格包含的数据集合，按照表格序号值（从1开始计算）为key，表格数据集合为value进行组织
     */
    private Map<Integer, ArrayList<String[]>> getTableDataForDoc() {
        Map<Integer, ArrayList<String[]>> tableDataMap = new LinkedHashMap<>();
        //得到文档的读取范围
        Range range = doc2003.getRange();
        TableIterator it = new TableIterator(range);
        int index = 1;
        while (it.hasNext()) {
            Table table = it.next();
            //迭代行，默认从0开始
            ArrayList<String[]> aTableText = getTableData(table);
            tableDataMap.put(index, aTableText);
            index++;
        }
        return tableDataMap;
    }

    /**
     * 将excel表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     *
     * @param table 表格元素
     * @return 表格包含的数据集合
     */
    private ArrayList<String[]> getTableData(Table table) {
        ArrayList<String[]> list = new ArrayList<>();
        for (int i = 0; i < table.numRows(); i++) {
            TableRow tr = table.getRow(i);
            String[] data = new String[tr.numCells()];
            for (int j = 0; j < tr.numCells(); j++) {
                TableCell td = tr.getCell(j);
                String text = td.text();
                data[j] = text.substring(0, text.length() - 1);
            }
            list.add(data);
        }
        return list;
    }

    /**
     * 将word文本内容逐段读取，该方法适合小页数范围的word内容获取，
     * 大页数范围的word内容获取使用该方法有可能导致内存占用过大
     * <p>
     * 注意：表格中的每个单元格会被认为是单独一个段落
     *
     * @return 按照段落顺序组织的文本内容集合
     */
    public ArrayList<String> getTextData() {
        if (doc2003 != null) {
            return getTextDataForDoc();
        } else if (doc2007 != null) {
            return getTextDataForDocx();
        }
        return null;
    }

    /**
     * 将word文本内容逐段读取，该方法适合小页数范围的word内容获取，
     * 大页数范围的word内容获取使用该方法有可能导致内存占用过大
     *
     * @return 按照段落顺序组织的文本内容集合
     */
    private ArrayList<String> getTextDataForDocx() {
        ArrayList<String> allTexts = new ArrayList<>();
        List<IBodyElement> elements = doc2007.getBodyElements();
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph) {
                String paragraphText = ((XWPFParagraph) element).getParagraphText();
                if (StringUtils.isNotBlank(paragraphText)) {
                    allTexts.add(paragraphText.trim());
                }
            } else if (element instanceof XWPFTable) {
                for (int i = 0, rows = ((XWPFTable) element).getNumberOfRows(); i < rows; i++) {
                    XWPFTableRow row = ((XWPFTable) element).getRow(i);
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        String text = cell.getText().trim();
                        if (!StringUtils.isEmpty(text)) {
                            allTexts.add(text);
                        }
                    }
                }
            }
        }
        return allTexts;
    }

    /**
     * 将word文本内容逐段读取，该方法适合小页数范围的word内容获取，
     * 大页数范围的word内容获取使用该方法有可能导致内存占用过大
     *
     * @return 按照段落顺序组织的文本内容集合
     */
    private ArrayList<String> getTextDataForDoc() {
        ArrayList<String> allTexts = new ArrayList<>();
        Range range = doc2003.getRange();
        int num = range.numParagraphs();
        Paragraph paragraph;
        for (int i = 0; i < num; i++) {
            paragraph = range.getParagraph(i);
            String text = paragraph.text().trim();
            if (!StringUtils.isEmpty(text)) {
                allTexts.add(text.trim());
            }
        }
        return allTexts;
    }

    /**
     * 绘制数据表格
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     */
    public void drawTable(Map<String, String> headers, Collection<Map<String, Object>> list, Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap, Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, String pattern) {
        // 时间格式默认"yyyy-MM-dd"
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (doc2003 != null) {
            drawTableForDoc(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, sdf);
        } else if (doc2007 != null) {
            drawTableForDocx(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, sdf);
        }
    }

    /**
     * 绘制数据表格
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param sdf               日期格式修饰器
     */
    private void drawTableForDocx(Map<String, String> headers, Collection<Map<String, Object>> list, Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap, Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, SimpleDateFormat sdf) {
        XWPFTable table = doc2007.createTable();
        // 设置表格总体宽度最大值
        table.setWidth(DEFAULT_MAX_WIDTH);
        int defaultCellWidth = DEFAULT_MAX_WIDTH / headers.size();
        drawTableHeader(table, headers, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, defaultCellWidth);
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        Iterator<Map<String, Object>> listIt = list.iterator();
        int row = 0;
        while (listIt.hasNext()) {
            row++;
            table.createRow();
            XWPFTableRow aRow = table.getRow(table.getNumberOfRows() - 1);
            // 设置行高
            setRowHeight(rowHeightStyleMap, aRow, row);
            Map<String, Object> map = listIt.next();
            // 列索引
            int cellNum = 0;
            // 遍历列名
            for (Map.Entry<String, String> next : entrySet) {
                Object value = map.get(next.getKey());
                XWPFTableCell cell = aRow.getCell(cellNum);
                setRowCellStyle(cell, row, cellNum, rowColorStyleMap, colWidthStyleMap, defaultCellWidth);
                if (imageColSet != null && imageColSet.contains(next.getKey()) && value != null) {
                    if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                        try {
                            setCellImage(cell, (String) value, aRow.getHeight(), colWidthStyleMap, cellNum, defaultCellWidth);
                        } catch (Exception e) {
                            log.error("SetCellImage:", e);
                            setCellContent(cell, sdf, value);
                        }
                    } else {
                        setCellContent(cell, sdf, value);
                    }
                } else {
                    setCellContent(cell, sdf, value);
                }
                cellNum++;
            }
        }
    }

    /**
     * 设置单元格图片内容
     *
     * @param imageCell        单元格对象
     * @param value            图片路径
     * @param rowHeight        单元格所在行高度
     * @param colWidthStyleMap 列的宽度风格
     * @param col              列号
     * @param defaultCellWidth 默认列宽，由表格的最大宽度按照列数进行等分计算获得
     * @throws IOException            处理过程中可能抛出该异常
     * @throws InvalidFormatException 处理过程中可能抛出该异常
     */
    private static void setCellImage(XWPFTableCell imageCell, String value, int rowHeight, Map<Integer, Integer> colWidthStyleMap, int col, int defaultCellWidth) throws IOException, InvalidFormatException {
        List<XWPFParagraph> paragraphs = imageCell.getParagraphs();
        XWPFParagraph newPara = paragraphs.get(0);
        XWPFRun imageCellRun = newPara.createRun();
        // 单元格宽度
        int width;
        if (colWidthStyleMap != null && colWidthStyleMap.get(col) != null) {
            width = colWidthStyleMap.get(col);
        } else {
            width = defaultCellWidth;
        }
        // 0.05为1/20，这里从单位长度换算为Point的值
        double w = width * 0.05;
        // 高度采用
        if (value.endsWith(KeyUtils.IMAGE_PNG)) {
            imageCellRun.addPicture(new FileInputStream(value), XWPFDocument.PICTURE_TYPE_PNG, "", Units.toEMU(w), Units.pixelToEMU(rowHeight));
        } else if (value.endsWith(KeyUtils.IMAGE_JPG) || value.endsWith(KeyUtils.IMAGE_JPEG)) {
            imageCellRun.addPicture(new FileInputStream(value), XWPFDocument.PICTURE_TYPE_JPEG, "", Units.toEMU(w), Units.pixelToEMU(rowHeight));
        } else if (value.endsWith(KeyUtils.IMAGE_BMP)) {
            imageCellRun.addPicture(new FileInputStream(value), XWPFDocument.PICTURE_TYPE_BMP, "", Units.toEMU(w), Units.pixelToEMU(rowHeight));
        }
    }

    /**
     * 设置单元格文本内容
     *
     * @param cell  单元格对象
     * @param sdf   日期格式器
     * @param value 单元格值
     */
    private void setCellContent(XWPFTableCell cell, SimpleDateFormat sdf, Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            String textValue = sdf.format(date);
            cell.setText(textValue);
        } else if (value instanceof String[]) {
            String[] strArr = (String[]) value;
            StringBuilder sb = new StringBuilder();
            for (String str : strArr) {
                if (StringUtils.isNotBlank(str)) {
                    sb.append(str).append(",");
                }
            }
            cell.setText(sb.substring(0, sb.length() - 1));
        } else if (value instanceof Double[]) {
            Double[] douArr = (Double[]) value;
            StringBuilder sb = new StringBuilder();
            for (Double val : douArr) {
                if (val != null) {
                    sb.append(val).append(",");
                }
            }
            cell.setText(sb.substring(0, sb.length() - 1));
        } else {
            // 其它数据类型都当作字符串简单处理
            String textValue = value == null ? "" : String.valueOf(value);
            cell.setText(textValue);
        }
    }

    /**
     * 设置行高
     *
     * @param rowHeightStyleMap 行的高度风格
     * @param row               行对象
     * @param rowNum            行号
     */
    private void setRowHeight(Map<Integer, Integer> rowHeightStyleMap, XWPFTableRow row, int rowNum) {
        if (rowHeightStyleMap != null && rowHeightStyleMap.get(rowNum) != null) {
            int height = rowHeightStyleMap.get(rowNum);
            row.setHeight(height);
        } else {
            // 采用默认行高
            row.setHeight(DEFAULT_ROW_HEIGHT);
        }
    }

    /**
     * 设置对应行单元格的高度与内容居中
     *
     * @param cell             表格单元格
     * @param row              所在行索引，从0开始计算
     * @param col              所在列索引，从0开始计算
     * @param rowColorStyleMap 行的背景颜色风格
     * @param colWidthStyleMap 列的宽度风格
     * @param defaultCellWidth 默认列宽，由表格的最大宽度按照列数进行等分计算获得
     */
    private static void setRowCellStyle(XWPFTableCell cell, int row, int col, Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> colWidthStyleMap, int defaultCellWidth) {
        // 设置居中
        CTTc cttc = cell.getCTTc();
        CTTcPr ctPr = cttc.addNewTcPr();
        ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
        cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
        // 设置背景颜色
        if (rowColorStyleMap != null && rowColorStyleMap.get(row) != null) {
            cell.setColor(rowColorStyleMap.get(row));
        }
        // 设置单元格宽度
        setCellWidth(colWidthStyleMap, cell, col, defaultCellWidth);
    }

    /**
     * 设置单元格宽度
     *
     * @param colWidthStyleMap 列的宽度风格
     * @param cell             单元格
     * @param col              列号
     * @param defaultCellWidth 默认列宽，由表格的最大宽度按照列数进行等分计算获得
     */
    private static void setCellWidth(Map<Integer, Integer> colWidthStyleMap, XWPFTableCell cell, int col, int defaultCellWidth) {
        CTTcPr cPr = cell.getCTTc().addNewTcPr();
        CTTblWidth width = cPr.addNewTcW();
        if (colWidthStyleMap != null && colWidthStyleMap.get(col) != null) {
            Integer colWidth = colWidthStyleMap.get(col);
            width.setW(BigInteger.valueOf(colWidth));
        } else {
            width.setW(BigInteger.valueOf(defaultCellWidth));
        }
    }

    /**
     * 绘制表格头
     *
     * @param table             表格对象
     * @param headers           表格属性列名数组
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param defaultCellWidth  默认列宽，由表格的最大宽度按照列数进行等分计算获得
     */
    private void drawTableHeader(XWPFTable table, Map<String, String> headers,
                                 Map<Integer, String> rowColorStyleMap,
                                 Map<Integer, Integer> rowHeightStyleMap, Map<Integer, Integer> colWidthStyleMap, int defaultCellWidth) {
        XWPFTableRow row = table.getRow(0);
        setRowHeight(rowHeightStyleMap, row, 0);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        // 改变长度策略为自己调整 默认为auto
        tblPr.getTblW().setType(STTblWidth.AUTO);
        // 创建剩余的cell，创建行时默认有一个单元格，为此这里从1开始计数
        for (int col = 1; col < headers.size(); col++) {
            row.createCell();
        }
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            XWPFTableCell cell = row.getCell(index);
            cell.setText(next.getValue());
            setRowCellStyle(cell, 0, index, rowColorStyleMap, colWidthStyleMap, defaultCellWidth);
            index++;
        }
    }

    /**
     * 绘制数据表格
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param sdf               日期格式修饰器
     */
    private void drawTableForDoc(Map<String, String> headers, Collection<Map<String, Object>> list, Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap, Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, SimpleDateFormat sdf) {
        Assert.isTrue(false, "Word 2003 is not supported at this stage!");
    }

    /**
     * 在指定行后面增加行
     *
     * @param tableIndex 表格索引，从0开始
     * @param rowIndex   行索引，从0开始
     */
    public void addTableRow(int tableIndex, int rowIndex) {
        Assert.isTrue(tableIndex >= 0, "The parameter tableIndex must be greater than or equal to 0.");
        Assert.isTrue(rowIndex >= 0, "The parameter rowIndex must be greater than or equal to 0.");
        if (doc2003 != null) {
            Assert.isTrue(false, "Word 2003 is not supported at this stage!");
        } else if (doc2007 != null) {
            addTableRow4Doc2007(tableIndex, rowIndex);
        } else if (doc2007plus != null) {
            Assert.isTrue(false, "Word 2007 plus is not supported at this stage!");
        }
    }

    /**
     * 在指定行后面增加行
     *
     * @param tableIndex 表格索引，从0开始
     * @param rowIndex   行索引，从0开始
     */
    private void addTableRow4Doc2007(int tableIndex, int rowIndex) {
        List<XWPFTable> tables = doc2007.getTables();
        XWPFTable table = tables.get(tableIndex);
        table.addRow(table.getRow(rowIndex), rowIndex);
    }

    /**
     * 合并表格指定区域的单元格
     *
     * @param tableIndex       表格索引，从0开始
     * @param cellRowFromIndex 起始单元格行索引，从0开始
     * @param cellColFromIndex 起始单元格列索引，从0开始
     * @param cellRowToIndex   目标单元格行索引，从0开始
     * @param cellColToIndex   目标单元格列索引，从0开始
     */
    public void mergeCell(int tableIndex, int cellRowFromIndex,
                          int cellColFromIndex, int cellRowToIndex, int cellColToIndex) {
        Assert.isTrue(tableIndex >= 0,
                "The parameter tableIndex must be greater than or equal to 0.");
        Assert.isTrue(cellRowFromIndex >= 0,
                "The parameter cellRowFromIndex must be greater than or equal to 0.");
        Assert.isTrue(cellColFromIndex >= 0,
                "The parameter cellColFromIndex must be greater than or equal to 0.");
        Assert.isTrue(cellRowToIndex >= 0,
                "The parameter cellRowToIndex must be greater than or equal to 0.");
        Assert.isTrue(cellColToIndex >= 0,
                "The parameter cellColToIndex must be greater than or equal to 0.");
        if (doc2003 != null) {
            Assert.isTrue(false, "Word 2003 is not supported at this stage!");
        } else if (doc2007 != null) {
            mergeCell4Doc2007(tableIndex, cellRowFromIndex,
                    cellColFromIndex, cellRowToIndex, cellColToIndex);
        } else if (doc2007plus != null) {
            Assert.isTrue(false, "Word 2007 plus is not supported at this stage!");
        }
    }

    /**
     * 合并表格指定区域的单元格
     *
     * @param tableIndex       表格索引，从0开始
     * @param cellRowFromIndex 起始单元格行索引，从0开始
     * @param cellColFromIndex 起始单元格列索引，从0开始
     * @param cellRowToIndex   目标单元格行索引，从0开始
     * @param cellColToIndex   目标单元格列索引，从0开始
     */
    private void mergeCell4Doc2007(int tableIndex, int cellRowFromIndex, int cellColFromIndex, int cellRowToIndex, int cellColToIndex) {
        List<XWPFTable> tables = doc2007.getTables();
        XWPFTable table = tables.get(tableIndex);
        if (cellRowFromIndex < cellRowToIndex) {
            mergeCellsVertically(table, cellColFromIndex, cellRowFromIndex, cellRowToIndex);
        }
        if (cellColFromIndex < cellColToIndex) {
            for (int row = cellRowFromIndex; row <= cellRowToIndex; row++) {
                mergeCellsHorizontal(table, row, cellColFromIndex, cellColToIndex);
            }
        }
    }

    /**
     * 单行水平方向跨列合并
     *
     * @param table            待处理表格对象
     * @param rowIndex         行索引
     * @param cellColFromIndex 列起始索引
     * @param cellColToIndex   列结束索引
     */
    private void mergeCellsHorizontal(XWPFTable table, int rowIndex, int cellColFromIndex, int cellColToIndex) {
        for (int cellIndex = cellColFromIndex; cellIndex <= cellColToIndex; cellIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(cellIndex);
            if (cellIndex == cellColFromIndex) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 单列垂直方向跨行合并
     *
     * @param table            待处理表格对象
     * @param colIndex         列索引
     * @param cellRowFromIndex 行起始索引
     * @param cellRowToIndex   行结束索引
     */
    private void mergeCellsVertically(XWPFTable table, int colIndex, int cellRowFromIndex, int cellRowToIndex) {
        for (int rowIndex = cellRowFromIndex; rowIndex <= cellRowToIndex; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(colIndex);
            if (rowIndex == cellRowFromIndex) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 获取表格指定单元格的文本内容
     *
     * @param tableIndex   表格索引，从0开始
     * @param cellRowIndex 行索引，从0开始
     * @param cellColIndex 列索引，从0开始
     * @return 单元格包含的文本值
     */
    public String getContentFromCell(int tableIndex, int cellRowIndex, int cellColIndex) {
        Assert.isTrue(tableIndex >= 0,
                "The parameter tableIndex must be greater than or equal to 0.");
        Assert.isTrue(cellRowIndex >= 0,
                "The parameter cellRowIndex must be greater than or equal to 0.");
        Assert.isTrue(cellColIndex >= 0,
                "The parameter cellColIndex must be greater than or equal to 0.");
        if (doc2003 != null) {
            Assert.isTrue(false, "Word 2003 is not supported at this stage!");
        } else if (doc2007 != null) {
            return getContentFromCell4Doc2007(tableIndex, cellRowIndex, cellColIndex);
        } else if (doc2007plus != null) {
            Assert.isTrue(false, "Word 2007 plus is not supported at this stage!");
        }
        return "";
    }

    /**
     * 获取表格指定单元格的文本内容
     *
     * @param tableIndex   表格索引，从0开始
     * @param cellRowIndex 行索引，从0开始
     * @param cellColIndex 列索引，从0开始
     * @return 单元格包含的文本值
     */
    private String getContentFromCell4Doc2007(int tableIndex, int cellRowIndex, int cellColIndex) {
        List<XWPFTable> tables = doc2007.getTables();
        XWPFTable table = tables.get(tableIndex);
        XWPFTableCell cell = table.getRow(cellRowIndex).getCell(cellColIndex);
        return cell.getText();
    }

    /**
     * 往表格指定单元格设置文本内容
     *
     * @param tableIndex   表格索引，从0开始
     * @param cellRowIndex 行索引，从0开始
     * @param cellColIndex 列索引，从0开始
     * @param cellValue    文本内容
     */
    public void inputTableCellContent(int tableIndex, int cellRowIndex, int cellColIndex, String cellValue) {
        Assert.isTrue(tableIndex >= 0,
                "The parameter tableIndex must be greater than or equal to 0.");
        Assert.isTrue(cellRowIndex >= 0,
                "The parameter cellRowIndex must be greater than or equal to 0.");
        Assert.isTrue(cellColIndex >= 0,
                "The parameter cellColIndex must be greater than or equal to 0.");
        if (doc2003 != null) {
            Assert.isTrue(false, "Word 2003 is not supported at this stage!");
        } else if (doc2007 != null) {
            inputTableCellContent4Doc2007(tableIndex, cellRowIndex, cellColIndex, cellValue);
        } else if (doc2007plus != null) {
            Assert.isTrue(false, "Word 2007 plus is not supported at this stage!");
        }
    }

    /**
     * 往表格指定单元格设置文本内容
     *
     * @param tableIndex   表格索引，从0开始
     * @param cellRowIndex 行索引，从0开始
     * @param cellColIndex 列索引，从0开始
     * @param cellValue    文本内容
     */
    private void inputTableCellContent4Doc2007(int tableIndex, int cellRowIndex, int cellColIndex, String cellValue) {
        log.debug("{},{},{},{}.", tableIndex, cellRowIndex, cellColIndex, cellValue);
        List<XWPFTable> tables = doc2007.getTables();
        XWPFTable table = tables.get(tableIndex);
        XWPFTableCell cell = table.getRow(cellRowIndex).getCell(cellColIndex);
        cell.setText(StringUtils.processNullStr(cellValue));
    }
}
