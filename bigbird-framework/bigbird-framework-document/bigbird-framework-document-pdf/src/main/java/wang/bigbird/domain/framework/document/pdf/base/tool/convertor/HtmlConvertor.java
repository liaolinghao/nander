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

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 基于Html的PDF文档转换器
 *
 * @author Bigbird
 */
@Slf4j
public class HtmlConvertor {

    /**
     * 将html文档转换为pdf文档
     *
     * @param htmlFile 源html文档
     * @param baseUri  基准uri，比如：本地~file:///C:/Users/HP/Desktop/html/，网络~http://xxx.xx/
     * @param fontMap  字体配置
     * @param destPdf  目标PDF文档
     */
    public static void html2Pdf(File htmlFile, String baseUri, Map<String, File> fontMap, File destPdf) {
        Assert.notNull(htmlFile,
                "The parameter htmlFile is null.");
        Assert.isTrue(htmlFile.exists(), "The parameter htmlFile is not existed.");
        Assert.isTrue(FileUtils.isHtmlFile(htmlFile), "The parameter htmlFile is not a valid html file.");
        html2Pdf(FileUtils.readContent(htmlFile, true), baseUri, fontMap, destPdf);
    }

    /**
     * 将html文档转换为pdf文档
     *
     * @param html    html文本
     * @param baseUri 基准uri，比如：本地~file:///C:/Users/HP/Desktop/html/，网络~http://xxx.xx/
     * @param fontMap 字体配置
     * @param destPdf 目标PDF文档
     */
    public static void html2Pdf(String html, String baseUri, Map<String, File> fontMap, File destPdf) {
        Assert.hasText(html, "The parameter html is blank.");
        Assert.notNull(destPdf,
                "The parameter destPdf is null.");
        Assert.isTrue(FileUtils.isPdfFile(destPdf), "The parameter destPdf is not a valid pdf file.");
        if (!destPdf.getParentFile().exists()) {
            destPdf.getParentFile().mkdirs();
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(destPdf);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            if (MapUtils.isNotEmpty(fontMap)) {
                fontMap.forEach((name, file) -> {
                    builder.useFont(file, name);
                });
            }
            builder.useFastMode();
            Document doc = Jsoup.parse(html);
            builder.withW3cDocument(new W3CDom().fromJsoup(doc), baseUri);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            log.error("Html2Pdf:", e);
        } finally {
            StreamUtils.close(os);
        }
    }

}
