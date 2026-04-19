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
package wang.bigbird.domain.framework.document.excel.support.office.microsoft.jacob;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.AbstractMicrosoftOfficeApp;
import wang.bigbird.domain.framework.document.excel.base.tool.ExcelProcessor;
import wang.bigbird.domain.framework.document.excel.support.office.microsoft.jacob.component.Workbook;
import wang.bigbird.domain.framework.document.excel.support.office.microsoft.jacob.component.Workbooks;

import java.io.File;

/**
 * Excel应用程序的JAVA操控器
 *
 * @author Bigbird
 */
public class ExcelApp extends AbstractMicrosoftOfficeApp {

    /**
     * Excel的工作表集合对象
     */
    private Workbooks workbooks;

    /**
     * 创建Excel应用程序
     */
    public ExcelApp() {

    }

    @Override
    protected void initialize() {
        // 初始化com的线程，开启多个word进程的支持
        ComThread.InitMTA(true);
        app = new ActiveXComponent("Excel.Application");
        app.setProperty("Visible", new Variant(false));
        Dispatch d = app.getProperty("Workbooks").toDispatch();
        workbooks = new Workbooks(app, d);
    }


    @Override
    protected void doConvert(File officeFile, File destFile) {
        ExcelProcessor.verifyFile(officeFile);
        Workbook wb = openExistDocument(officeFile);
        if (FileUtils.isPdfFile(destFile)) {
            wb.saveAsPdf(destFile.getAbsolutePath());
        } else if (FileUtils.isHtmlFile(destFile)) {
            wb.saveAsHtml(destFile.getAbsolutePath());
        }
        wb.close();
    }

    @Override
    public Workbook openExistDocument(File officeFile) {
        ExcelProcessor.verifyFile(officeFile);
        Dispatch d = Dispatch.call(workbooks.getDispatch(), "Open",
                officeFile.getAbsolutePath()).toDispatch();
        return new Workbook(app, d);
    }

}
