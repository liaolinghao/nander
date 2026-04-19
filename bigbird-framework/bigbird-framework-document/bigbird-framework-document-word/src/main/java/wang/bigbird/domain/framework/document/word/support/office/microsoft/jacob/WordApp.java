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
package wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.AbstractMicrosoftOfficeApp;
import wang.bigbird.domain.framework.document.word.base.tool.WordProcessor;
import wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.component.Document;
import wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.component.Documents;
import wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.component.Selection;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Word应用程序的JAVA操控器
 *
 * @author Bigbird
 */
public class WordApp extends AbstractMicrosoftOfficeApp {

    /**
     * 页眉对象类型值
     */
    private final static int DOC_HEADER = 9;
    /**
     * 页脚对象类型值
     */
    private final static int DOC_FOOTER = 10;

    /**
     * Word的文档集合对象
     */
    private Documents documents;

    /**
     * 该对象代表窗口或窗格中的当前所选内容。
     * 所选内容代表文档中被选定（或突出显示的）的区域，若文档中没有所选内容，则代表插入点。
     * 每个文档窗格只能有一个活动的 Selection对象，并且整个应用程序中只能有一个活动的 Selection对象。
     */
    private Selection selection;

    /**
     * 创建Word应用程序
     */
    public WordApp() {

    }

    @Override
    protected void initialize() {
        // 初始化com的线程，开启多个word进程的支持
        ComThread.InitMTA(true);
        app = new ActiveXComponent("Word.Application");
        app.setProperty("Visible", new Variant(false));
        documents = new Documents(app, app.getProperty("Documents").toDispatch());
    }

    @Override
    protected void doConvert(File officeFile, File destFile) {
        WordProcessor.verifyFile(officeFile);
        Document doc = openExistDocument(officeFile);
        if (FileUtils.isPdfFile(destFile)) {
            doc.saveAsPdf(destFile.getAbsolutePath());
        } else if (FileUtils.isHtmlFile(destFile)) {
            doc.saveAsHtml(destFile.getAbsolutePath());
        }
        doc.close();
    }

    @Override
    public Document openExistDocument(File officeFile) {
        WordProcessor.verifyFile(officeFile);
        Dispatch d = Dispatch.call(documents.getDispatch(), "Open",
                officeFile.getAbsolutePath()).toDispatch();
        return new Document(app, d);
    }

    /**
     * 获取Selection对象
     *
     * @return
     */
    private Selection getSelection() {
        if (selection == null) {
            selection = new Selection(app, Dispatch.call(app, "Selection").toDispatch());
        }
        return selection;
    }

    /**
     * 替换文档的页眉与页脚中的文本
     *
     * @param doc   待处理的文档
     * @param texts 文本数据
     */
    public void replaceHeaderFooterText(Document doc, LinkedHashMap<String, String> texts) {
        Assert.notNull(doc, "The parameter doc is null.");
        if (CollectionUtils.isNullOrEmpty(texts)) {
            return;
        }
        Dispatch activeWindow = Dispatch.get(doc.getDispatch(), "ActiveWindow")
                .toDispatch();
        // 取得视窗对象
        Dispatch view = Dispatch.get(activeWindow, "View").toDispatch();
        replaceHeaderFooterText(view, texts, DOC_HEADER);
        replaceHeaderFooterText(view, texts, DOC_FOOTER);
        Dispatch.put(view, "SeekView", new Variant(0));
    }

    /**
     * 替换页眉中的文本
     *
     * @param view  视窗对象
     * @param texts 文本数据
     * @param type  页眉或者页脚的对象类型值
     */
    private void replaceHeaderFooterText(Dispatch view, LinkedHashMap<String, String> texts, int type) {
        Dispatch.put(view, "SeekView", new Variant(type));
        Dispatch header = Dispatch.get(getSelection().getDispatch(),
                "HeaderFooter").toDispatch();
        Iterator<String> it = texts.keySet().iterator();
        while (it.hasNext()) {
            String obj = it.next();
            // 当前选中的页眉对象
            Dispatch hRange = Dispatch.get(header, "Range").toDispatch();
            Selection st = new Selection(app, hRange);
            st.replaceText(obj, texts.get(obj));
        }
    }

    /**
     * 替换文档正文中的文本
     *
     * @param texts 文本数据
     */
    public void replaceDocText(LinkedHashMap<String, String> texts) {
        if (CollectionUtils.isNullOrEmpty(texts)) {
            return;
        }
        Iterator<String> it = texts.keySet().iterator();
        while (it.hasNext()) {
            String obj = it.next();
            getSelection().moveStart();
            getSelection().replaceAllText(obj, texts.get(obj));
        }
    }

    /**
     * 往文档指定表格指定单元格录入数据
     *
     * @param doc          待处理的文档
     * @param tableIndex   表格索引，从1开始
     * @param cellRowIndex 行索引，从1开始
     * @param cellColIndex 列索引，从1开始
     * @param content      文本值
     */
    public void inputTableCellContent(Document doc, int tableIndex, int cellRowIndex,
                                      int cellColIndex, String content) {
        Assert.notNull(doc,
                "The parameter doc is null.");
        Assert.isTrue(tableIndex > 0,
                "The parameter tableIndex must be greater than 0.");
        Assert.isTrue(cellRowIndex > 0,
                "The parameter cellRowIndex must be greater than 0.");
        Assert.isTrue(cellColIndex > 0,
                "The parameter cellColIndex must be greater than 0.");
        Dispatch tables = Dispatch.get(doc.getDispatch(), "Tables").toDispatch();
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableIndex))
                .toDispatch();
        Dispatch cell = Dispatch.call(table, "Cell",
                cellRowIndex, cellColIndex)
                .toDispatch();
        Dispatch.call(cell, "Select");
        Dispatch.put(getSelection().getDispatch(), "Text", StringUtils.processNullStr(content));
    }

    /**
     * 在指定文本标签处插入图片
     *
     * @param textMark  文本标签
     * @param imageFile 图片文件
     */
    public void insertImageAtTextMark(String textMark, File imageFile) {
        Assert.notNull(textMark,
                "The parameter oldText is null.");
        Assert.notNull(imageFile,
                "The parameter imageFile is null.");
        Assert.isTrue(imageFile.exists(),
                "The parameter imageFile is not existed.");
        Assert.isTrue(FileUtils.isImageFile(imageFile),
                "The parameter imageFile is not a valid image file.");
        getSelection().insertImageAtTextMark(textMark, imageFile);
    }
}
