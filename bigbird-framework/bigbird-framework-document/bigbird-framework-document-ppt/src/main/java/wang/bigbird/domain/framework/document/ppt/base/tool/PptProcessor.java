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
package wang.bigbird.domain.framework.document.ppt.base.tool;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;

import java.io.File;

/**
 * Ppt文档处理器，提供围绕Ppt处理的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public class PptProcessor {

    private PptProcessor() {
    }

    /**
     * 验证文件有效性
     *
     * @param file excel文件
     */
    public static void verifyFile(File file) {
        Assert.notNull(file,
                "The parameter file is null.");
        Assert.isTrue(file.exists() && FileUtils.isPowerPointFile(file), "The parameter file is not a valid ppt file.");
    }

}
