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
package wang.bigbird.domain.framework.document.ppt.support.office.microsoft.jacob;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.AbstractMicrosoftOfficeApp;
import wang.bigbird.domain.framework.document.ppt.base.tool.PptProcessor;
import wang.bigbird.domain.framework.document.ppt.support.office.microsoft.jacob.component.Presentation;
import wang.bigbird.domain.framework.document.ppt.support.office.microsoft.jacob.component.Presentations;

import java.io.File;

/**
 * Ppt应用程序的JAVA操控器
 *
 * @author Bigbird
 */
public class PptApp extends AbstractMicrosoftOfficeApp {

    /**
     * Ppt的幻灯片集合对象
     */
    private Presentations presentations;

    /**
     * 创建PPT应用程序
     */
    public PptApp() {

    }

    @Override
    protected void initialize() {
        // 初始化com的线程，开启多个word进程的支持
        ComThread.InitMTA(true);
        app = new ActiveXComponent("PowerPoint.Application");
        // 禁用宏
        app.setProperty("AutomationSecurity", new Variant(3));
        Dispatch d = app.getProperty("Presentations").toDispatch();
        presentations = new Presentations(app, d);
    }

    @Override
    protected void doConvert(File officeFile, File destFile) {
        PptProcessor.verifyFile(officeFile);
        Presentation ppt = openExistDocument(officeFile);
        if (FileUtils.isPdfFile(destFile)) {
            ppt.saveAsPdf(destFile.getAbsolutePath());
        } else if (FileUtils.isHtmlFile(destFile)) {
            ppt.saveAsHtml(destFile.getAbsolutePath());
        }
        ppt.close();
    }

    @Override
    public Presentation openExistDocument(File officeFile) {
        PptProcessor.verifyFile(officeFile);
        Dispatch d = Dispatch.call(presentations.getDispatch(), "Open",
                officeFile.getAbsolutePath()).toDispatch();
        return new Presentation(app, d);
    }

}
