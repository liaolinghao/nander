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
package wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component.Bridge;

import java.io.File;

/**
 * Microsoft Office应用程序操控器，仅支持WINDOWS系统并且依赖Microsoft Office应用程序的COM组件。
 * 在技术上，该操控器采用jacob技术，依赖jacob.jar和jacob-1.19-x64.dll（在64位window系统）
 * 因此，需要把hola-docface-api.jar包内lib目录内的jacob-1.19-x64.dll放到依赖的64位JDK BIN目录下，
 * 如：C:\Program Files\Java\jdk1.8.0_291\bin 和 C:\Program Files\Java\jdk1.8.0_291\jre\bin
 *
 * @author Bigbird
 */
public abstract class AbstractMicrosoftOfficeApp {

    /**
     * office中各种文档格式
     */
    protected static final String[] OFFICE_POSTFIXS = {"doc", "docx", "xls",
            "xlsx", "ppt", "pptx"};

    /**
     * Microsoft Office应用程序本身
     */
    protected ActiveXComponent app;

    /**
     * 创建office应用程序时进行初始化
     */
    public AbstractMicrosoftOfficeApp() {
        initialize();
    }

    /**
     * 应用退出
     */
    public void quit() {
        app.invoke("Quit", new Variant[]{});
        app.safeRelease();
        ComThread.Release();
    }

    /**
     * 把office文档转换成目标文档
     *
     * @param officeFile office文档
     * @param destFile   目标文档，目前仅支持PDF和HTML
     */
    public void convert(File officeFile, File destFile) {
        Assert.notNull(officeFile,
                "The parameter officeFile is null.");
        Assert.isTrue(officeFile.exists() && FileUtils.isSpecifiedTypeFile(officeFile, OFFICE_POSTFIXS), "The parameter officeFile is not a valid office file.");
        Assert.notNull(destFile,
                "The parameter destFile is null.");
        Assert.isTrue(FileUtils.isPdfFile(destFile) || FileUtils.isHtmlFile(destFile),
                "The destFile is not supported.");
        doConvert(officeFile, destFile);
    }

    /**
     * 初始化应用
     */
    protected abstract void initialize();

    /**
     * 执行把office文档转换成目标文档
     *
     * @param officeFile office文档
     * @param destFile   目标文档，目前仅支持PDF和HTML
     */
    protected abstract void doConvert(File officeFile, File destFile);

    /**
     * 打开一个已存在的文档
     *
     * @param officeFile office文档
     * @return 当前操控的单份文档对应的Microsoft Office应用程序的控制权，
     * 此时Microsoft Office应用程序成为了JAVA程序的提线木偶，JAVA程序通过该控制权操控Microsoft Office应用程序完成对文档的编辑处理
     */
    public abstract Bridge openExistDocument(File officeFile);
}
