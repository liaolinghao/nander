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
package wang.bigbird.domain.framework.document.pdf.base.tool.convertor;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.AbstractMicrosoftOfficeApp;
import wang.bigbird.domain.framework.document.excel.support.office.microsoft.jacob.ExcelApp;
import wang.bigbird.domain.framework.document.ppt.support.office.microsoft.jacob.PptApp;
import wang.bigbird.domain.framework.document.word.support.office.microsoft.jacob.WordApp;

import java.io.File;

/**
 * 基于Jacob调用MicrosoftOffice组件的PDF文档转换器，仅支持Windows平台，转换原理为：
 * <p>
 * 机器上需要安装MicrosoftOffice，利用MicrosoftOffice的进程执行文档格式转换，一个进程同一时刻只能转换一个文档。
 * 不支持多进程。
 * 需要把依赖的 hola-docface-api jar包内resources目录下lib目录内的jacob-1.19-x64.dll放到依赖的64位JDK BIN目录下，
 * 如：C:\Program Files\Java\jdk1.8.0_291\bin 和 C:\Program Files\Java\jdk1.8.0_291\jre\bin
 * <p>
 * 经过实践，目前依靠MicrosoftOffice进行PDF文档转换，得到的文档质量与手工操作MicrosoftOffice进行转换质量一致。
 * 缺点：转换速度较慢，如果MicrosoftOffice进程发生异常，转换可能失败。
 * 如果用户对office文档格式有极其严格的要求，可以考虑采用该组件。
 * 另外，由于不支持多进程，为提高转换速度，可以考虑部署多台windows转换服务，将多份文档的转换任务分配到不同服务上同时进行。
 *
 * @author Bigbird
 */
@Slf4j
public class MicrosoftOfficeConvertor extends OfficeConvertor {


    /**
     * 禁止构造转化器实例
     */
    private MicrosoftOfficeConvertor() {
    }

    /**
     * 利用MicrosoftOffice将office文档转换为pdf文档，其过程为：
     *
     * @param officeFile 源office文档
     * @param destPdf    目标PDF文档
     */
    public static void office2Pdf(File officeFile, File destPdf) {
        verifyConvertFiles(officeFile, destPdf);
        if (!destPdf.getParentFile().exists()) {
            destPdf.getParentFile().mkdirs();
        }
        AbstractMicrosoftOfficeApp officeApp = null;
        try {
            if (FileUtils.isExcelFile(officeFile)) {
                officeApp = new ExcelApp();
            } else if (FileUtils.isWordFile(officeFile)) {
                officeApp = new WordApp();
            } else {
                officeApp = new PptApp();
            }
            officeApp.convert(officeFile, destPdf);
        } finally {
            if (officeApp != null) {
                officeApp.quit();
            }
        }
    }

}
