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
package wang.bigbird.domain.framework.document.html.base.tool;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Html文档处理器，提供围绕Html处理的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public class HtmlProcessor {

    /**
     * 防止类实例化
     */
    private HtmlProcessor() {
    }

    /**
     * 验证文件有效性
     *
     * @param file excel文件
     */
    public static void verifyFile(File file) {
        Assert.notNull(file,
                "The parameter file is null.");
        Assert.isTrue(file.exists() && FileUtils.isHtmlFile(file), "The parameter file is not a valid html file.");
    }

    /**
     * 将数据填充进html模版生成html文本内容
     *
     * @param templateFile html模版文件
     * @param data         数据
     * @return html文本内容
     * @throws IOException
     */
    public static String generateHtml(File templateFile, Map<String, Object> data) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        // 配置模版加载路径
        cfg.setDirectoryForTemplateLoading(templateFile.getParentFile());
        // 获取模版
        Template template = cfg.getTemplate(templateFile.getName());
        StringWriter writer = new StringWriter();
        try {
            template.process(data, writer);
        } catch (TemplateException te) {
            log.error("GenerateHtml:", te);
            throw new IOException(te.getMessage());
        }
        return writer.toString();
    }


}
