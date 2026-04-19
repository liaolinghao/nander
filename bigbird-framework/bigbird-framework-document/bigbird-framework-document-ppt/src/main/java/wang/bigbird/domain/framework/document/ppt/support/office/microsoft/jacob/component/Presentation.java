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
package wang.bigbird.domain.framework.document.ppt.support.office.microsoft.jacob.component;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.document.api.support.office.microsoft.jacob.component.AbstractInstance;


/**
 * 代表当前操控的单份文档的控制权
 *
 * @author Bigbird
 */
public class Presentation extends AbstractInstance {

    public Presentation(ActiveXComponent app, Dispatch dispatch) {
        super(app, dispatch);
    }

    @Override
    public void saveAs(String filePathName) {
        Assert.notNull(filePathName,
                "The parameter filePathName is null.");
        Assert.isTrue(FileUtils.isPowerPointFile(filePathName), "The parameter filePathName is not a valid ppt file.");
        Dispatch.call(dispatch, "SaveAs", filePathName);
    }

    @Override
    public void doSaveAsHtml(String htmlFilePath) {
        Dispatch.invoke(dispatch, "SaveAs", Dispatch.Method, new Object[]{
                htmlFilePath, new Variant(12)}, new int[1]);
    }

    @Override
    public void doSaveAsPdf(String pdfFilePath) {
        Dispatch.call(dispatch, "SaveAs",
                new Object[]{pdfFilePath, Integer.valueOf(32)});
    }

    @Override
    public void save() {
        Dispatch.call(dispatch, "Save");
    }

    @Override
    public void close() {
        Dispatch.call(dispatch, "Close");
    }

}
