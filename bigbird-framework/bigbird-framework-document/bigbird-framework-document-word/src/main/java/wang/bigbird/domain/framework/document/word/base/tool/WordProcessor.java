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
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Word文档处理器，提供围绕Word处理的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public class WordProcessor {

    /**
     * 2003格式的word文件
     */
    public static final String EXTENSION_DOC = "doc";
    /**
     * 2007格式的word文件
     */
    public static final String EXTENSION_DOCX = "docx";

    /**
     * 防止类实例化
     */
    private WordProcessor() {
    }

    /**
     * 验证文件有效性
     *
     * @param file excel文件
     */
    public static void verifyFile(File file) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.isTrue(file.exists() && FileUtils.isWordFile(file), "The parameter file is not a valid word file.");
    }

    /***
     * 取得DocumentWrapper对象，该对象采用装饰器模式将支持2003格式的word处理器和2007格式的word处理器进行了包装，
     * 以获得统一风格的编码风格
     *
     * @param file word文档
     * @return word文档对应的DocumentWrapper对象
     * @throws IOException 文档读取过程中，可能发生异常
     *
     */
    public static WordDocument getDocument(File file) throws IOException {
        return getDocument(file, false);
    }

    /**
     * 取得DocumentWrapper对象，该对象采用装饰器模式将支持2003格式的word处理器和2007格式的word处理器进行了包装，
     * 以获得统一风格的编码风格
     *
     * @param file word文档
     * @param plus 是否采用增强版处理器
     * @return word文档对应的DocumentWrapper对象
     * @throws IOException 文档读取过程中，可能发生异常
     */
    public static WordDocument getDocument(File file, boolean plus) throws IOException {
        verifyFile(file);
        WordDocument document = null;
        InputStream is = new FileInputStream(file);
        if (file.getName().endsWith(EXTENSION_DOC)) {
            document = new WordDocument(new HWPFDocument(is));
        } else if (file.getName().endsWith(EXTENSION_DOCX)) {
            if (plus) {
                document = new WordDocument(XWPFTemplate.compile(is));
            } else {
                document = new WordDocument(new XWPFDocument(is));
            }
        }
        return document;
    }

    /**
     * 将word表格的内容按照最简单的行列模式，构造二维集合返回，集合中一个元素代表一行数据
     * 注意，该方法不会读取WORD文档中表格里面的图片数据，也不会读取表格之外的数据。
     * 备注：WORD文档中，表格数据类型的单元格定位，不存在类似EXCEL采用物理单元格为计算体系，
     * WORD表格单元格定位按照从上到下，以1为起点计算行索引，
     * 每一行中按照从左到右，以1为起点计算列索引，从而定位单元格位置。
     * 这意味着WORD每一行包含的单元格数量可能不同，为此复杂表格返回的表格数据中，每一行对应的数组大小可能不相等。
     *
     * @param file word文档
     * @return word文档中所有表格包含的数据集合，按照表格序号值（从1开始计算）为key，表格数据集合为value进行组织
     */
    public static Map<Integer, ArrayList<String[]>> getTableData(File file) throws IOException {
        verifyFile(file);
        WordDocument document = null;
        try {
            document = getDocument(file);
            return document.getTableData();
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * 将word文本内容逐段读取，该方法适合小页数范围的word内容获取，
     * 大页数范围的word内容获取使用该方法有可能导致内存占用过大
     * <p>
     * 注意：表格中的每个单元格会被认为是单独一个段落
     *
     * @param file word文档
     * @return 按照段落顺序组织的文本内容集合
     */
    public static ArrayList<String> getTextData(File file) throws IOException {
        verifyFile(file);
        WordDocument document = null;
        try {
            document = getDocument(file);
            return document.getTextData();
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers 表格属性列名数组
     * @param list    需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param out     与输出设备关联的流对象，可以将Word文档导出到本地文件或者网络中
     * @param type    word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(LinkedHashMap<String, String> headers, Collection<Map<String, Object>> list,
                                  OutputStream out, String type) throws IOException {
        exportWord(headers, list, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers          表格属性列名数组
     * @param list             需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                         javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap 行的背景颜色风格，使用String作为背景颜色值，传入16进制网页色格式，如：F9FAFA
     * @param out              与输出设备关联的流对象，可以将Word文档导出到本地文件或者网络中
     * @param type             word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(LinkedHashMap<String, String> headers, Collection<Map<String, Object>> list,
                                  Map<Integer, String> rowColorStyleMap,
                                  OutputStream out, String type) throws IOException {
        exportWord(headers, list, rowColorStyleMap, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，使用String作为背景颜色值，传入16进制网页色格式，如：F9FAFA
     * @param rowHeightStyleMap 行的高度风格
     * @param out               与输出设备关联的流对象，可以将Word文档导出到本地文件或者网络中
     * @param type              word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(LinkedHashMap<String, String> headers, Collection<Map<String, Object>> list,
                                  Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap,
                                  OutputStream out, String type) throws IOException {
        exportWord(headers, list, rowColorStyleMap, rowHeightStyleMap, null, out, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，使用String作为背景颜色值，传入16进制网页色格式，如：F9FAFA
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param out               与输出设备关联的流对象，可以将Word文档导出到本地文件或者网络中
     * @param type              word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(LinkedHashMap<String, String> headers, Collection<Map<String, Object>> list,
                                  Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap,
                                  Map<Integer, Integer> colWidthStyleMap, OutputStream out, String type) throws IOException {
        exportWord(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, null, out, null, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，使用String作为背景颜色值，传入16进制网页色格式，如：F9FAFA
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格，指定列需要占几个字符宽度
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param type              word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(Map<String, String> headers, Collection<Map<String, Object>> list,
                                  Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap,
                                  Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String type)
            throws IOException {
        exportWord(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, out, null, type);
    }

    /**
     * 将放置在JAVA集合中并且符合一定条件的数据以WORD的形式输出到指定IO设备上，用于单个简易表格输出
     *
     * @param headers           表格属性列名数组
     * @param list              需要显示的数据集合，集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                          javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param rowColorStyleMap  行的背景颜色风格，使用String作为背景颜色值，传入16进制网页色格式，如：F9FAFA
     * @param rowHeightStyleMap 行的高度风格
     * @param colWidthStyleMap  列的宽度风格
     * @param imageColSet       图片列，以标题为标识
     * @param out               与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern           如果有时间数据，设定输出格式。默认为"yyyy-MM-dd"
     * @param type              word文件类型
     * @throws IOException 文档处理过程中，可能发生异常
     */
    public static void exportWord(Map<String, String> headers, Collection<Map<String, Object>> list,
                                  Map<Integer, String> rowColorStyleMap, Map<Integer, Integer> rowHeightStyleMap,
                                  Map<Integer, Integer> colWidthStyleMap, Set<String> imageColSet, OutputStream out, String pattern,
                                  String type) throws IOException {
        Assert.notNull(headers,
                "The parameter headers is null.");
        Assert.notNull(list,
                "The parameter list is null.");
        Assert.notNull(out,
                "The parameter out is null.");
        WordDocument document = null;
        if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(EXTENSION_DOC)) {
            Assert.isTrue(false,  "Word 2003 format is not supported at this stage!");
        } else {
            document = new WordDocument(new XWPFDocument());
        }
        try {
            document.drawTable(headers, list, rowColorStyleMap, rowHeightStyleMap, colWidthStyleMap, imageColSet, pattern);
            document.write(out);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

}
