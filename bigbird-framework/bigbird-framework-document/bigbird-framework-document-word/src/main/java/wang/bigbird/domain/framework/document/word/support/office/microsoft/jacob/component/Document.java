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
package wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component.AbstractInstance;

import java.io.File;

/**
 * 代表当前操控的单份文档的控制权
 *
 * @author Bigbird
 */
public class Document extends AbstractInstance {

    public Document(ActiveXComponent app, Dispatch dispatch) {
        super(app, dispatch);
    }

    @Override
    public void saveAs(String filePathName) {
        Assert.notNull(filePathName,
                "The parameter filePathName is null.");
        Assert.isTrue(FileUtils.isWordFile(filePathName), "The parameter filePathName is not a valid word file.");
        Dispatch.call(dispatch, "SaveAs", filePathName);
    }

    @Override
    public void doSaveAsHtml(String htmlFilePath) {
        Dispatch.invoke(dispatch, "SaveAs", Dispatch.Method, new Object[]{
                htmlFilePath, new Variant(8)}, new int[1]);
    }

    @Override
    public void doSaveAsPdf(String pdfFilePath) {
        Dispatch.invoke(dispatch, "SaveAs", Dispatch.Method, new Object[]{
                pdfFilePath, new Variant(17)}, new int[1]);
    }

    @Override
    public void save() {
        Dispatch.call(dispatch, "Save");
    }

    @Override
    public void close() {
        Dispatch.call(dispatch, "Close", new Variant(true));
    }

    /**
     * 获取文档内对应表格对应单元格的内容
     *
     * @param tableIndex   表格索引，从1开始
     * @param cellRowIndex 行索引，从1开始
     * @param cellColIndex 列索引，从1开始
     * @return
     */
    public String getContentFromCell(int tableIndex, int cellRowIndex,
                                     int cellColIndex) {
        Assert.isTrue(tableIndex > 0,
                "The parameter tableIndex must be greater than 0.");
        Assert.isTrue(cellRowIndex > 0,
                "The parameter cellRowIndex must be greater than 0.");
        Assert.isTrue(cellColIndex > 0,
                "The parameter cellColIndex must be greater than 0.");
        // 所有表格
        Dispatch tables = Dispatch.get(dispatch, "Tables").toDispatch();
        // 要填充的表格
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableIndex))
                .toDispatch();
        Dispatch cell = Dispatch.call(table, "Cell", new Variant(cellRowIndex),
                new Variant(cellColIndex)).toDispatch();
        Dispatch range = Dispatch.get(cell, "Range").toDispatch();
        String ret = Dispatch.get(range, "Text").toString();
        // 去掉最后的回车符
        ret = ret.substring(0, ret.length() - 2);
        return ret;
    }

    /**
     * 在指定行前面增加行
     *
     * @param tableIndex 表格索引，从1开始
     * @param rowIndex   行索引，从1开始
     */
    public void addTableRow(int tableIndex, int rowIndex) {
        Assert.isTrue(tableIndex > 0,
                "The parameter tableIndex must be greater than 0.");
        Assert.isTrue(rowIndex > 0,
                "The parameter rowIndex must be greater than 0.");
        Dispatch tables = Dispatch.get(dispatch, "Tables").toDispatch();
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableIndex))
                .toDispatch();
        Dispatch rows = Dispatch.get(table, "Rows").toDispatch();
        Dispatch row = Dispatch.call(rows, "Item", new Variant(rowIndex))
                .toDispatch();
        Dispatch.call(rows, "Add", new Variant(row));
    }

    /**
     * 合并表格指定区域的单元格
     *
     * @param tableIndex       表格索引，从1开始
     * @param cellRowFromIndex 起始单元格行索引，从1开始
     * @param cellColFromIndex 起始单元格列索引，从1开始
     * @param cellRowToIndex   目标单元格行索引，从1开始
     * @param cellColToIndex   目标单元格列索引，从1开始
     */
    public void mergeCell(int tableIndex, int cellRowFromIndex,
                          int cellColFromIndex, int cellRowToIndex, int cellColToIndex) {
        Assert.isTrue(tableIndex > 0,
                "The parameter tableIndex must be greater than 0.");
        Assert.isTrue(cellRowFromIndex > 0,
                "The parameter cellRowFromIndex must be greater than 0.");
        Assert.isTrue(cellColFromIndex > 0,
                "The parameter cellColFromIndex must be greater than 0.");
        Assert.isTrue(cellRowToIndex > 0,
                "The parameter cellRowToIndex must be greater than 0.");
        Assert.isTrue(cellColToIndex > 0,
                "The parameter cellColToIndex must be greater than 0.");
        // 所有表格
        Dispatch tables = Dispatch.get(dispatch, "Tables").toDispatch();
        // 要填充的表格
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableIndex))
                .toDispatch();
        Dispatch fromCell = Dispatch.call(table, "Cell",
                new Variant(cellRowFromIndex), new Variant(cellColFromIndex))
                .toDispatch();
        Dispatch toCell = Dispatch.call(table, "Cell",
                new Variant(cellRowToIndex), new Variant(cellColToIndex))
                .toDispatch();
        Dispatch.call(fromCell, "Merge", toCell);
    }


    /**
     * 在指定书签插入图片
     *
     * @param bookmarkName 书签名称
     * @param imageFile    图片文件
     */
    @SuppressWarnings("deprecation")
    public void insertImageAtBookMark(String bookmarkName, File imageFile) {
        Assert.notNull(bookmarkName,
                "The parameter bookmarkName is null.");
        Assert.notNull(imageFile,
                "The parameter imageFile is null.");
        Assert.isTrue(imageFile.exists(),
                "The parameter imageFile is not existed.");
        Assert.isTrue(FileUtils.isImageFile(imageFile),
                "The parameter imageFile is not a valid image file.");
        Dispatch activeDoc = app.getProperty("ActiveDocument").toDispatch();
        Dispatch bookMarks = Dispatch.call(activeDoc, "Bookmarks").toDispatch();
        boolean bookMarkExist = Dispatch
                .call(bookMarks, "Exists", bookmarkName).toBoolean();
        if (bookMarkExist) {
            Dispatch rangeItem = Dispatch.call(bookMarks, "Item", bookmarkName)
                    .toDispatch();
            Dispatch range = Dispatch.call(rangeItem, "Range").toDispatch();
            Dispatch.call(Dispatch.get(range, "InLineShapes").toDispatch(),
                    "AddPicture", imageFile.getAbsolutePath());
        }
    }


}
