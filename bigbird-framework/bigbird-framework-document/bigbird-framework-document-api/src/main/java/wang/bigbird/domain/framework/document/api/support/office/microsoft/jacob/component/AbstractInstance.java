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
package wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;


/**
 * 代表打开的一份可操控的Microsoft Office文档实例
 *
 * @author Bigbird
 */
public abstract class AbstractInstance extends Bridge {

    public AbstractInstance(ActiveXComponent app, Dispatch dispatch) {
        super(app, dispatch);
    }

    /**
     * 文档另存为到新的地址
     *
     * @param filePathName 新的文档保存路径
     */
    public abstract void saveAs(String filePathName);

    /**
     * 文档保存为html
     *
     * @param htmlFilePath html文档地址
     */
    public void saveAsHtml(String htmlFilePath) {
        Assert.notNull(htmlFilePath,
                "The parameter htmlFilePath is null.");
        Assert.isTrue(FileUtils.isHtmlFile(htmlFilePath), "The parameter htmlFilePath is not a valid html file.");
        doSaveAsHtml(htmlFilePath);
    }

    /**
     * 文档保存为html
     *
     * @param htmlFilePath html文档地址
     */
    public abstract void doSaveAsHtml(String htmlFilePath);

    /**
     * 文档保存为pdf
     *
     * @param pdfFilePath pdf文档地址
     */
    public void saveAsPdf(String pdfFilePath) {
        Assert.notNull(pdfFilePath, "The parameter pdfFilePath is null.");
        Assert.isTrue(FileUtils.isPdfFile(pdfFilePath), "The parameter pdfFilePath is not a valid pdf file.");
        doSaveAsPdf(pdfFilePath);
    }

    /**
     * 文档保存为pdf
     *
     * @param pdfFilePath pdf文档地址
     */
    public abstract void doSaveAsPdf(String pdfFilePath);

    /**
     * 保存文档更改的内容
     */
    public abstract void save();

    /**
     * 关闭文档
     */
    public abstract void close();
}
