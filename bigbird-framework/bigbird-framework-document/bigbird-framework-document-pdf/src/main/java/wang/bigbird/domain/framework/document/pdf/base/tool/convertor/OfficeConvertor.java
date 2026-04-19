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

import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;

import java.io.File;

/**
 * 基于Office的PDF文档转换器
 *
 * @author Bigbird
 */
public class OfficeConvertor {

    /**
     * office中各种文档格式
     */
    protected static final String[] OFFICE_POSTFIXS = {"doc", "docx", "xls",
            "xlsx", "ppt", "pptx"};

    /**
     * 验证转换文件
     *
     * @param officeFile 源office文档
     * @param destPdf    目标PDF文档
     */
    protected static void verifyConvertFiles(File officeFile, File destPdf) {
        Assert.notNull(officeFile,
                "The parameter officeFile is null.");
        Assert.notNull(destPdf,
                "The parameter destPdf is null.");
        Assert.isTrue(officeFile.exists(), "The parameter officeFile is not existed.");
        Assert.isTrue(FileUtils.isSpecifiedTypeFile(officeFile, OFFICE_POSTFIXS), "The parameter officeFile is not a valid office file.");
        Assert.isTrue(FileUtils.isPdfFile(destPdf), "The parameter destPdf is not a valid pdf file.");
    }

}
