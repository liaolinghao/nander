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
package wang.bigbird.domain.framework.document.excel.support.handler;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 大数据量excel处理器，一般数据量超10万，但是对于部分excel文件仍然会出现内存溢出问题，该现象于2018年5月9日进行授权数据迁移恢复时发现，
 * 且未找到解决方法
 *
 * @author Bigbird
 */
public abstract class AbstractExcelHandler extends DefaultHandler {

    private static Logger logger = LoggerFactory.getLogger(AbstractExcelHandler.class);

    private static final String CELL_ELEMENT = "c";
    private static final String VALUE_ELEMENT = "v";
    private static final String ROW_ELEMENT = "row";
    private static final String STRING_TYPE_ATTRIBUTE = "s";

    private SharedStringsTable sst;
    private String lastContents;
    private boolean nextIsString;

    private final List<String> rowList = new ArrayList<>();

    /**
     * 记录正在处理的工作簿索引
     */
    private int sheetIndex = -1;
    /**
     * 记录正在处理的行索引
     */
    private int curRow = 0;
    /**
     * 记录正在处理的列索引
     */
    private int curCol = 0;
    /**
     * 是否读取单元格数值
     */
    private boolean isReadValue = false;

    /**
     * 读取指定工作簿的入口方法
     *
     * @param path
     * @param relId 工作簿的标识，可以把xlsx改名为zip文件解压后再从workbook.xml中查看
     * @throws Exception
     */
    public void readOneSheet(String path, String relId) {
        InputStream sheet = null;
        try {
            OPCPackage pkg = OPCPackage.open(path);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();
            XMLReader parser = fetchSheetParser(sst);
            sheet = r.getSheet(relId);
            curRow = 0;
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
        } catch (InvalidFormatException e) {
            logger.error("ReadOneSheet:", e);
        } catch (IOException e) {
            logger.error("ReadOneSheet:", e);
        } catch (OpenXML4JException e) {
            logger.error("ReadOneSheet:", e);
        } catch (SAXException e) {
            logger.error("ReadOneSheet:", e);
        } finally {
            if (sheet != null) {
                try {
                    sheet.close();
                } catch (IOException e) {
                    logger.error("Close:", e);
                }
            }
        }
    }

    /**
     * 读取所有工作簿的入口方法
     *
     * @param path
     * @throws Exception
     */
    public void readAllSheet(String path) {
        try {
            OPCPackage pkg = OPCPackage.open(path);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();
            XMLReader parser = fetchSheetParser(sst);
            Iterator<InputStream> sheets = r.getSheetsData();
            while (sheets.hasNext()) {
                curRow = 0;
                sheetIndex++;
                InputStream sheet = null;
                try {
                    sheet = sheets.next();
                    InputSource sheetSource = new InputSource(sheet);
                    parser.parse(sheetSource);
                } catch (Exception e) {
                    logger.error("ReadSheet:", e);
                } finally {
                    if (sheet != null) {
                        try {
                            sheet.close();
                        } catch (IOException e) {
                            logger.error("Close:", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ReadAllSheet:", e);
        }
    }

    /**
     * 获取表格的处理器
     *
     * @param sst
     * @return
     * @throws SAXException
     */
    public XMLReader fetchSheetParser(SharedStringsTable sst)
            throws SAXException {
        XMLReader parser = XMLReaderFactory
                .createXMLReader("org.apache.xerces.parsers.SAXParser");
        this.sst = sst;
        parser.setContentHandler(this);
        return parser;
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) {
        // 单元格
        if (CELL_ELEMENT.equals(name)) {
            // 如果下一个元素是SST的索引，则将nextIsString标记为true
            String cellType = attributes.getValue("t");
            if (cellType != null) {
                nextIsString = STRING_TYPE_ATTRIBUTE.equals(cellType);
            } else {
                nextIsString = false;
            }
            isReadValue = false;
        }
        lastContents = "";
    }

    @Override
    public void endElement(String uri, String localName, String name) {
        // 根据SST的索引值到单元格的真正要存储的字符串
        // 这时characters()方法可能会被调用多次
        if (nextIsString) {
            try {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx))
                        .toString();
            } catch (Exception e) {

            }
        }
        // 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
        // 将单元格内容加入rowList中，在这之前先去掉字符串前后的空白符
        if (VALUE_ELEMENT.equals(name)) {
            String value = lastContents.trim();
            value = "".equals(value) ? " " : value;
            rowList.add(curCol, value);
            curCol++;
            isReadValue = true;
        } else {
            if (CELL_ELEMENT.equals(name)) {
                // 空单元格的数值存储
                if (!isReadValue) {
                    rowList.add(curCol, "");
                    curCol++;
                }
            } else if (ROW_ELEMENT.equals(name)) {
                // 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
                optRow(sheetIndex, curRow, rowList);
                rowList.clear();
                curRow++;
                curCol = 0;
            }
        }
    }

    /**
     * 得到单元格内容的值
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        lastContents += new String(ch, start, length);
    }

    /**
     * 该方法自动被调用，每读一行调用一次，在方法中写自己的业务逻辑即可
     *
     * @param sheetIndex 工作簿序号
     * @param curRow     处理到第几行
     * @param rowList    当前数据行的数据集合
     */
    public abstract void optRow(int sheetIndex, int curRow, List<String> rowList);

}
